grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    repositories {
        grailsPlugins()
        grailsHome()
        mavenCentral()
        mavenRepo name: 'Codehaus', root: 'http://repository.codehaus.org', m2compatible: true
    }
    dependencies {
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.1') {
            excludes "commons-logging", "xml-apis", "groovy"
        }
    }
    plugins {
        test(":spock:0.5-groovy-1.7") {
            export = false
        }

    }
}
