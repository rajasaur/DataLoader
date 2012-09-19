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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector

/**
 * Works around the fact that the class is dynamically compiled and not from a file.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
  *
 * @since 0.1
 */
class DynamicDomainClassInjector extends DefaultGrailsDomainClassInjector {

	// always true since we're only compiling dynamic domain classes
	@Override
	boolean shouldInject(URL url) { true }

	// always true since we're only compiling dynamic domain classes
	@Override
	protected boolean isDomainClass(ClassNode cn, SourceUnit su) { true }
}
