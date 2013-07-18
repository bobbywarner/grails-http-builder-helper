grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenRepo name: 'Codehaus', root: 'http://repository.codehaus.org', m2compatible: true
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.6') {
            excludes "commons-logging", "xml-apis", "groovy"
        }
    }

    plugins {
        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
