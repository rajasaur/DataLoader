import org.codehaus.groovy.grails.commons.ApplicationAttributes


class BootStrap {

    def grailsApplication
    def dataLoaderService

    def init = { servletContext ->
        new File(grailsApplication.config.xls.store.folder).listFiles(new FileFilter()).each { file ->
            def domainText = file.getText()
            dataLoaderService.addToDomain(domainText)
        }
    }

    def destroy = {
    }
}

class FileFilter implements FilenameFilter {
    public boolean accept(File f, String filename) {
        return filename.endsWith(".txt")
    }
}
