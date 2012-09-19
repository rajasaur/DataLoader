package com.rajasaur.dataloader

import com.lucastex.grails.fileuploader.UFile
import com.rajasaur.dataloader.excel.ExcelBuilder

class FileUploadController {

    def grailsApplication
    def dataLoaderService

    def index = {
        if (params.ufileId) {
            log.debug "Uploaded file with id=${params.ufileId}"
            def ufile = UFile.get(params.ufileId)
            log.debug "Path is ${ufile.path}"
            File sourceFile = new File(ufile.path)
            def dst = copy(sourceFile, new File(grailsApplication.config.xls.store.folder))

            def indexOfDotInFileName = ufile.name.indexOf(".")
            def outputClass = ufile.name.substring(0, indexOfDotInFileName)

            boolean exists = grailsApplication.getArtefacts("Domain").findAll {
                outputClass.capitalize().equals(it.getName())
            }

            if (exists) {
                flash.message = "excel.already.exists"
                [domains:  getModels()]
            } else {
                def fields = []

                new ExcelBuilder(dst.path).eachLine { row ->
                    if (row.getRowNum() == 0) {
                        def cellIterator = row.cellIterator()

                        while(cellIterator.hasNext()) {
                            def cell = cellIterator.next()
                            fields << cell
                        }
                    }
                }
                [fields: fields, name: outputClass, processed: true]
            }
        } else {
            [domains: getModels()]
        }
    }

    def getModels = {
        def models = [:] as HashMap<String, String>
        grailsApplication.getArtefacts("Domain").each {
            if (!"UFile".equals(it.getName())) {
                models["com.rajasaur.dataloader.xls." + it.getName()] = it.getName()
            }
        }
        models
    }

    def processFields = {
        def fileName = params.name
        def fields = params.selectedFields

        def lineSep = System.getProperty("line.separator")
        def fileContents = new StringBuilder("package com.rajasaur.dataloader.xls")
        fileContents << lineSep
        fileContents << "class ${fileName.capitalize()} {"
        fileContents << lineSep

        fields.each { fld ->
            def translatedField = fld.replaceAll(/[\. ;]/, "_")
            fileContents << "\tString ${translatedField}"; fileContents << lineSep;
        }
        fileContents << "}"
        new File(grailsApplication.config.xls.store.folder, "${fileName}.txt").write(fileContents.toString())

        // REgister with the manager
        def contents = new File(grailsApplication.config.xls.store.folder, "${fileName}.txt").getText()
        dataLoaderService.addToDomain(contents)

        flash.message = "data.processed.success"
        redirect(action: "index")

    }

    def copy = {File src, File dst ->
        if (dst.isDirectory()) {
            dst = new File(dst, src.getName())
        }

        def input = src.newDataInputStream()
        def output = dst.newDataOutputStream()

        output << input

        input.close()
        output.close()
        dst
    }

}
