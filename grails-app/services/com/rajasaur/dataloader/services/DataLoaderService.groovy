package com.rajasaur.dataloader.services

import org.codehaus.groovy.grails.commons.ApplicationAttributes

class DataLoaderService {

    def dynamicDomainService
    def grailsApplication

    static transactional = true

    def addToDomain(String fileContents) {
        dynamicDomainService.registerDomainClass(fileContents)
        dynamicDomainService.updateSessionFactory grailsApplication.mainContext
    }
}
