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
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.pages.FastStringWriter

  /**
 * Work as runtime component of renderEditor.template.
 * 
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
class RenderEditor {
	void render(out, domainClass, property) {
		if (property.type == Boolean.class || property.type == boolean.class)
			out << applyTemplate(renderBooleanEditor(domainClass, property))
		else if (Number.class.isAssignableFrom(property.type) || (property.type.isPrimitive() && property.type != boolean.class))
			out << applyTemplate(renderNumberEditor(domainClass, property))
		else if (property.type == String.class)
			out << applyTemplate(renderStringEditor(domainClass, property))
		else if (property.type == Date.class || property.type == java.sql.Date.class || property.type == java.sql.Time.class || property.type == Calendar.class)
			out << applyTemplate(renderDateEditor(domainClass, property))
		else if (property.type == URL.class)
			out << applyTemplate(renderStringEditor(domainClass, property))
		else if (property.isEnum())
			out << applyTemplate(renderEnumEditor(domainClass, property))
		else if (property.type == TimeZone.class)
			out << applyTemplate(renderSelectTypeEditor("timeZone", domainClass, property))
		else if (property.type == Locale.class)
			out << applyTemplate(renderSelectTypeEditor("locale", domainClass, property))
		else if (property.type == Currency.class)
			out << applyTemplate(renderSelectTypeEditor("currency", domainClass, property))
		else if (property.type==([] as Byte[]).class) //TODO: Bug in groovy means i have to do this :(
			out << applyTemplate(renderByteArrayEditor(domainClass, property))
		else if (property.type==([] as byte[]).class) //TODO: Bug in groovy means i have to do this :(
			out << applyTemplate(renderByteArrayEditor(domainClass, property))
		else if (property.manyToOne || property.oneToOne)
			out << applyTemplate(renderManyToOne(domainClass, property))
		else if ((property.oneToMany && !property.bidirectional) || (property.manyToMany && property.isOwningSide()))
			out << applyTemplate(renderManyToMany(domainClass, property))
		else if (property.oneToMany)
			out << applyTemplate(renderOneToMany(domainClass, property))		
	}
	
	private renderEnumEditor(domainClass, property) {
		return "<g:select name=\"${property.name}\" from=\"\${${property.type.name}?.values()}\" keys=\"\${${property.type.name}?.values()*.name()}\" value=\"\${domainInstance?.${property.name}?.name()}\" ${renderNoSelection(property)} />"
	}
	
	private renderStringEditor(domainClass, property) {
		def cp = domainClass.constrainedProperties[property.name]
		if (!cp) {
			return "<g:textField name=\"${property.name}\" value=\"\${domainInstance?.${property.name}}\" />"
		}
		else {
			if ("textarea" == cp.widget || (cp.maxSize > 250 && !cp.password && !cp.inList)) {
				return "<g:textArea name=\"${property.name}\" cols=\"40\" rows=\"5\" value=\"\${domainInstance?.${property.name}}\" />"
			}
			else {
				if (cp.inList) {
					return "<g:select name=\"${property.name}\" from=\"\${domainInstance.constraints.${property.name}.inList}\" value=\"\${domainInstance?.${property.name}}\" valueMessagePrefix=\"${domainClass.propertyName}.${property?.name}\" ${renderNoSelection(property)} />"
				}
				else {
					def sb = new StringBuffer("<g:")
					cp.password ? sb << "passwordField " : sb << "textField "
					sb << "name=\"${property.name}\" "
					if (cp.maxSize) sb << "maxlength=\"${cp.maxSize}\" "
					if (!cp.editable) sb << "readonly=\"readonly\" "
					sb << "value=\"\${domainInstance?.${property.name}}\" />"
					return sb.toString()
				}
			}
		}
	}
	
	private renderByteArrayEditor(domainClass, property) {
		return "<input type=\"file\" id=\"${property.name}\" name=\"${property.name}\" />"
	}
	
	private renderManyToOne(domainClass,property) {
		if (property.association) {
			return "<g:select name=\"${property.name}.id\" from=\"\${grailsApplication.getDomainClass('${property.type.name}').clazz.list()}\" optionKey=\"id\" value=\"\${domainInstance?.${property.name}?.id}\" ${renderNoSelection(property)} />"
		}
	}
	
	private renderManyToMany(domainClass, property) {
		return "<g:select name=\"${property.name}\" from=\"\${grailsApplication.getDomainClass('${property.referencedDomainClass.fullName}').clazz.list()}\" multiple=\"yes\" optionKey=\"id\" size=\"5\" value=\"\${domainInstance?.${property.name}*.id}\" />"
	}
	
	private renderOneToMany(domainClass, property) {
		def sw = new StringWriter()
		def pw = new PrintWriter(sw)
		pw.println()
		pw.println "<ul>"
		pw.println "<g:each in=\"\${domainInstance?.${property.name}?}\" var=\"${property.name[0]}\">"
		pw.println "    <li><g:link controller=\"${property.referencedDomainClass.propertyName}\" action=\"show\" id=\"\${${property.name[0]}.id}\">\${${property.name[0]}?.encodeAsHTML()}</g:link></li>"
		pw.println "</g:each>"
		pw.println "</ul>"
		pw.println "<g:link controller=\"${property.referencedDomainClass.propertyName}\" action=\"create\" params=\"['${domainClass.propertyName}.id': domainInstance?.id]\">\${message(code: 'default.add.label', args: [message(code: '${property.referencedDomainClass.propertyName}.label', default: '${property.referencedDomainClass.shortName}')])}</g:link>"
		return sw.toString()
	}
	
	private renderNumberEditor(domainClass, property) {
		def cp = domainClass.constrainedProperties[property.name]
		if (!cp) {
			if (property.type == Byte.class) {
				return "<g:select name=\"${property.name}\" from=\"\${-128..127}\" value=\"\${fieldValue(bean: domainInstance, field: '${property.name}')}\" />"
			}
			else {
				return "<g:textField name=\"${property.name}\" value=\"\${fieldValue(bean: domainInstance, field: '${property.name}')}\" />"
			}
		}
		else {
			if (cp.range) {
				return "<g:select name=\"${property.name}\" from=\"\${${cp.range.from}..${cp.range.to}}\" value=\"\${fieldValue(bean: domainInstance, field: '${property.name}')}\" ${renderNoSelection(property)} />"
			}
			else if (cp.inList) {
				return "<g:select name=\"${property.name}\" from=\"\${domainInstance.constraints.${property.name}.inList}\" value=\"\${fieldValue(bean: domainInstance, field: '${property.name}')}\" valueMessagePrefix=\"${domainClass.propertyName}.${property?.name}\" ${renderNoSelection(property)} />"
			}            
			else {
				return "<g:textField name=\"${property.name}\" value=\"\${fieldValue(bean: domainInstance, field: '${property.name}')}\" />"
			}
		}
	}
	
	private renderBooleanEditor(domainClass, property) {
		def cp = domainClass.constrainedProperties[property.name]
		if (!cp) {
			return "<g:checkBox name=\"${property.name}\" value=\"\${domainInstance?.${property.name}}\" />"
		}
		else {
			def sb = new StringBuffer("<g:checkBox name=\"${property.name}\" ")
			if (cp.widget) sb << "widget=\"${cp.widget}\" ";
			cp.attributes.each { k, v ->
				sb << "${k}=\"${v}\" "
			}
			sb << "value=\"\${domainInstance?.${property.name}}\" />"
			return sb.toString()
		}
	}
	
	private renderDateEditor(domainClass, property) {
		def cp = domainClass.constrainedProperties[property.name]
		def precision = (property.type == Date.class || property.type == java.sql.Date.class || property.type == Calendar.class) ? "day" : "minute";
		if (!cp) {
			return "<g:datePicker name=\"${property.name}\" precision=\"${precision}\" value=\"\${domainInstance?.${property.name}}\" />"
		}
		else {
			if (!cp.editable) {
				return "\${domainInstance?.${property.name}?.toString()}"
			}
			else {
				def sb = new StringBuffer("<g:datePicker name=\"${property.name}\" ")
				if (cp.format) sb << "format=\"${cp.format}\" "
				if (cp.widget) sb << "widget=\"${cp.widget}\" "
				cp.attributes.each { k, v ->
					sb << "${k}=\"${v}\" "
				}
				sb << "precision=\"${precision}\" value=\"\${domainInstance?.${property.name}}\" ${renderNoSelection(property)} />"
				return sb.toString()
			}
		}
	}
	
	private renderSelectTypeEditor(type, domainClass,property) {
		def cp = domainClass.constrainedProperties[property.name]
		if (!cp) {
			return "<g:${type}Select name=\"${property.name}\" value=\"\${domainInstance?.${property.name}}\" />"
		}
		else {
			def sb = new StringBuffer("<g:${type}Select name=\"${property.name}\" ")
			if (cp.widget) sb << "widget=\"${cp.widget}\" ";
			cp.attributes.each { k, v ->
				sb << "${k}=\"${v}\" "
			}
			sb << "value=\"\${domainInstance?.${property.name}}\" ${renderNoSelection(property)} />"
			return sb.toString()
		}
	}
	
	private renderNoSelection(property) {
		if (property.optional) {
			if (property.manyToOne || property.oneToOne) {
				return "noSelection=\"['null': '']\""
			}
			else {
				return "noSelection=\"['': '']\""
			}
		}
		return ""
	}
	
	// codes taken from org.codehaus.groovy.grails.web.taglib.AbstractGrailsTagTests
	def applyTemplate(template, params = [:], target = null, String filename = null ) {
		GroovyPagesTemplateEngine engine = AH.application.mainContext.groovyPagesTemplateEngine
		
		def t = engine.createTemplate(template, filename ?: "renderEditor_"+ System.currentTimeMillis())
		
		def w = t.make(params)
		
		if (!target) {
			target = new FastStringWriter()
		}
		
		w.writeTo(target)
		
		return target.toString()
	}
}
