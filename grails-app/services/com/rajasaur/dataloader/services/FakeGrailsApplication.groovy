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

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * This is needed when calling DomainClassGrailsPlugin.enhanceDomainClasses()
 * and HibernatePluginSupport.enhanceSessionFactory() so they only act
 * on the new class.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
  *
 * @since 0.1
 */

class FakeGrailsApplication extends DefaultGrailsApplication {

   final domainClasses

   FakeGrailsApplication(GrailsDomainClass dc) {
      super([dc.clazz] as Class[], AH.application.classLoader)
      domainClasses = [dc]
   }
}