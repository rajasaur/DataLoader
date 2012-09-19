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
package com.rajasaur.dataloader

/**
 * Dynamic Controller for Dynamic Domain Class.
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
class DdcController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	static final List DOMAIN_CLASS_SYSTEM_FIELDS = ["id", "version", "dateCreated", "lastUpdated"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[domainInstanceList: domainClass.clazz.list(params),
		domainInstanceTotal: domainClass.clazz.count(),
		domainClass: domainClass]
	}

	def create = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.newInstance()
		//TODO domainInstance.properties = params
		return [domainInstance: domainInstance, domainClass:domainClass,
		multiPart:false] // multiPart:true if form have upload component
	}

	def save = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.newInstance()
		setProperties(domainClass, domainInstance, params)
		if (domainInstance.validate()) {
			domainInstance.save(flush: true)
			flash.message = "${message(code: 'default.created.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), domainInstance.id])}"
			redirect(action: "show", id: domainInstance.id, params:[dc:params.dc])
		}
		else {
			render(view: "create", model: [domainInstance: domainInstance, domainClass: domainClass])
		}
	}

	// Replacement of domainInstance.properties = params, http://jira.codehaus.org/browse/GRAILS-1601
	private void setProperties(domainClass, domainInstance, params) {
		[domainClass.properties.findAll {
			!(it.name in DOMAIN_CLASS_SYSTEM_FIELDS)
		}].flatten().each { p ->
			if (p.type == Boolean.class || p.type == boolean.class) {
				domainInstance."${p.name}" = false
			}
			if (params[p.name] || params[p.name] == '') {
				if (p.oneToMany || p.manyToMany) {
					[params[p.name]].flatten().each { id ->
						domainInstance."${p.name}" << p.referencedDomainClass.clazz.get(id)
					}
				} else if (p.manyToOne || p.oneToOne) {
					domainInstance."${p.name}" = p.referencedDomainClass.clazz.get(params[p.name])
				} else {
					domainInstance."${p.name}" = params[p.name]
				}
			}
		}
	}

	def show = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.clazz.get(params.id)
		if (!domainInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
			redirect(action: "list", params:[dc:params.dc])
		}
		else {
			[domainInstance: domainInstance, domainClass: domainClass]
		}
	}

	def edit = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.clazz.get(params.id)
		if (!domainInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
			redirect(action: "list", params:[dc:params.dc])
		}
		else {
			return [domainInstance: domainInstance, domainClass: domainClass]
		}
	}

	def update = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.clazz.get(params.id)
		if (domainInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (domainInstance.version > version) {
					domainInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: '${domainClass.propertyName}.label', default: domainClass.name)] as Object[], "Another user has updated this ${domainClass.propertyName} while you were editing")
					render(view: "edit", model: [domainInstance: domainInstance, domainClass: domainClass])
					return
				}
			}
			setProperties(domainClass, domainInstance, params)
			if (!domainInstance.hasErrors() && domainInstance.validate()) {
				domainInstance.save(flush: true)
				flash.message = "${message(code: 'default.updated.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), domainInstance.id])}"
				redirect(action: "show", id: domainInstance.id, params:[dc:params.dc])
			}
			else {
				render(view: "edit", model: [domainInstance: domainInstance, domainClass: domainClass])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
			redirect(action: "list", params:[dc:params.dc])
		}
	}

	def delete = {
		def domainClass = grailsApplication.getDomainClass(params.dc)
		def domainInstance = domainClass.clazz.get(params.id)
		if (domainInstance) {
			try {
				domainInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
				redirect(action: "list", params:[dc:params.dc])
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
				redirect(action: "show", id: params.id, params:[dc:params.dc])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: domainClass.name), params.id])}"
			redirect(action: "list", params:[dc:params.dc])
		}
	}
}
