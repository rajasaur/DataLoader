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

import java.security.CodeSource

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.compiler.injection.ClassInjector
import org.codehaus.groovy.grails.compiler.injection.GrailsAwareClassLoader
import org.codehaus.groovy.grails.compiler.injection.GrailsAwareInjectionOperation
import org.springframework.util.ReflectionUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

/**
 * Uses a custom injector.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
   *
 * @since 0.1
 */
class DynamicClassLoader extends GrailsAwareClassLoader {

	private final ClassInjector[] _classInjectors = [new DynamicDomainClassInjector()]

	DynamicClassLoader() {
		super(ApplicationHolder.application.classLoader, CompilerConfiguration.DEFAULT)
		classInjectors = _classInjectors
		setClassLoader()
	}

	@Override
	protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
		CompilationUnit cu = super.createCompilationUnit(config, source)
		cu.addPhaseOperation(new GrailsAwareInjectionOperation(
			getResourceLoader(), _classInjectors), Phases.CANONICALIZATION)
		cu
	}

	// Register class to classLoader's private loadedClasses field
	private void setClassLoader() {
			def field = ReflectionUtils.findField(DefaultGrailsApplication.class, "cl")
			field.accessible = true
			// def classLoader = field.get(ApplicationHolder.application)
	    field.set ApplicationHolder.application, this
   }
}
