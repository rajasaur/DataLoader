/* Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rajasaur.dataloader.services

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.compiler.injection.ClassInjector
import org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector
import org.codehaus.groovy.grails.compiler.injection.GrailsAwareClassLoader
import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.codehaus.groovy.grails.plugins.orm.hibernate.HibernatePluginSupport
import org.codehaus.groovy.grails.validation.GrailsDomainClassValidator

import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.beans.factory.config.RuntimeBeanReference
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.util.ReflectionUtils
import org.codehaus.groovy.grails.plugins.ValidationGrailsPlugin

/**
 * Compiles and registers domain classes at runtime.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
  *@author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
  *
 * @since 0.1
 */
class DynamicDomainService {
  DynamicClassLoader dynamicClassLoader = new DynamicClassLoader()

	void registerDomainClass(String code) {
		def application = AH.application

		def clazz = compile(code, application)

		// register it as if it was a class under grails-app/domain
		//GrailsDomainClass dc = application.addArtefact(DomainClassArtefactHandler.TYPE, clazz)
        GrailsDomainClass dc = addDomainClass(clazz)

		def ctx = application.mainContext

		registerBeans ctx, dc

		enhanceDomainClass ctx, dc
	}

	private GrailsDomainClass addDomainClass(Class clazz) {
		GrailsDomainClass dc
		def application = AH.application
		if (application.getDomainClass(clazz.name)) {
			application.addOverridableArtefact(clazz) // support class update
			dc = application.getDomainClass(clazz.name)
		} else {
			dc = application.addArtefact(DomainClassArtefactHandler.TYPE, clazz)
		}
		return dc
	}

	private Class compile(String code, application) {
		Class clazz = dynamicClassLoader.parseClass(code)
		// application.classLoader.setClassCacheEntry clazz // TODO hack
		clazz
	}

	// this is typically done in DomainClassGrailsPlugin.doWithSpring
	private void registerBeans(ctx, GrailsDomainClass dc) {

		ctx.registerBeanDefinition dc.fullName,
			new GenericBeanDefinition(
				beanClass: dc.clazz,
				scope: AbstractBeanDefinition.SCOPE_PROTOTYPE,
				autowireMode:AbstractBeanDefinition.AUTOWIRE_BY_NAME)

		GenericBeanDefinition beanDef = new GenericBeanDefinition(
			beanClass: MethodInvokingFactoryBean,
			lazyInit: true)
		setBeanProperty beanDef, 'targetObject', new RuntimeBeanReference('grailsApplication', true)
		setBeanProperty beanDef, 'targetMethod', 'getArtefact'
		setBeanProperty beanDef, 'arguments', [DomainClassArtefactHandler.TYPE, dc.fullName]
		ctx.registerBeanDefinition "${dc.fullName}DomainClass", beanDef

		beanDef = new GenericBeanDefinition(
			beanClass: MethodInvokingFactoryBean,
			lazyInit: true)
		setBeanProperty beanDef, 'targetObject', new RuntimeBeanReference("${dc.fullName}DomainClass")
		setBeanProperty beanDef, 'targetMethod', 'getClazz'
		ctx.registerBeanDefinition "${dc.fullName}PersistentClass", beanDef

		beanDef = new GenericBeanDefinition(
			beanClass: GrailsDomainClassValidator,
			lazyInit: true)
		setBeanProperty beanDef, 'messageSource', new RuntimeBeanReference('messageSource')
		setBeanProperty beanDef, 'domainClass', new RuntimeBeanReference("${dc.fullName}DomainClass")
		setBeanProperty beanDef, 'grailsApplication', new RuntimeBeanReference('grailsApplication', true)
		ctx.registerBeanDefinition "${dc.fullName}Validator", beanDef
	}

	private void setBeanProperty(GenericBeanDefinition bean, String name, value) {
		bean.propertyValues.addPropertyValue name, value
	}


	private void enhanceDomainClass(ctx, GrailsDomainClass dc) {
		def fakeGrailsApplication = new FakeGrailsApplication(dc)
		DomainClassGrailsPlugin.enhanceDomainClasses(fakeGrailsApplication, ctx)
		HibernatePluginSupport.enhanceSessionFactory(ctx.sessionFactory, fakeGrailsApplication, ctx)
		ValidationGrailsPlugin.addValidationMethods(AH.application, dc.clazz, ctx)
	}
	// creates a new session factory so new classes can reference existing, and then replaces
	// the data in the original session factory with the new combined data
	void updateSessionFactory(ctx) {
		def sessionFactoryBean = ctx.getBean('&sessionFactory')
		def newSessionFactoryFactory = new ConfigurableLocalSessionFactoryBean(
			dataSource: ctx.dataSource,
			configLocations: getFieldValue(sessionFactoryBean, 'configLocations'),
			configClass: getFieldValue(sessionFactoryBean, 'configClass'),
			hibernateProperties: getFieldValue(sessionFactoryBean, 'hibernateProperties'),
			grailsApplication: ctx.grailsApplication,
			lobHandler: getFieldValue(sessionFactoryBean, 'lobHandler'),
			entityInterceptor: getFieldValue(sessionFactoryBean, 'entityInterceptor'))

		newSessionFactoryFactory.afterPropertiesSet()

		def newSessionFactory = newSessionFactoryFactory.object
		// newSessionFactoryFactory.updateDatabaseSchema()

		['entityPersisters', 'collectionPersisters', 'identifierGenerators',
		 'namedQueries', 'namedSqlQueries', 'sqlResultSetMappings', 'imports',
		 'collectionRolesByEntityParticipant', 'classMetadata', 'collectionMetadata'].each { fieldName ->
			def field = ReflectionUtils.findField(ctx.sessionFactory.getClass(), fieldName)
			field.accessible = true
			field.set ctx.sessionFactory, new HashMap(field.get(newSessionFactory))
		}
	}

	private getFieldValue(sessionFactoryBean, String fieldName) {
		def field = ReflectionUtils.findField(sessionFactoryBean.getClass(), fieldName)
		field.accessible = true
		field.get sessionFactoryBean
	}
}

