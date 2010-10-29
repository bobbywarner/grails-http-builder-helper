grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"


grails.project.dependency.resolution = {
    inherits('global') 
    log 'warn'
    repositories {
        grailsPlugins()
        grailsHome()
        // grailsCentral()
        mavenCentral()
        mavenRepo name: 'Sonatype', root: 'https://repository.sonatype.org/content/groups/public', m2compatible: true
    }
    dependencies {
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0-RC2') {exclude 'xercesImpl'}
        compile('xerces:xercesImpl:2.9.1') { exclude 'xml-apis'}
        runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0-RC2') {exclude 'xercesImpl'}
        runtime('xerces:xercesImpl:2.9.1') { exclude 'xml-apis'}
    }
}
