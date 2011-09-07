/*
 * Copyright 2009-2010 the original author or authors.
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

import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.plugins.rest.RestClientFixture
import org.grails.plugins.rest.RestConfig

/**
 * @author Andres.Almiray , Bernardo.GomezPalacio
 */
class RestGrailsPlugin {
    // the plugin version
    def version = "0.7.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/UrlMappings.groovy",
            "grails-app/views/",
            "web-app/"
    ]

    def author = "Andres Almiray, Bernardo Gomez-Palacio"
    def authorEmail = "aalmiray@users.sourceforge.net, bernardo.gomezpalacio@gmail.com"
    def title = "REST client facilities"
    def description = '''
Adds REST client capabilities to your Grails application.
'''
    def observe = ['controllers', 'services']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/Rest+Plugin"

    def doWithDynamicMethods = { ctx ->
        processArtifacts()
    }

    def onChange = { event ->
        processArtifacts()
    }

    def onConfigChange = { event ->
        processArtifacts()
    }

    private processArtifacts() {
        final def applicationContext = ApplicationHolder.application
        RestConfig restConfig = RestClientFixture.getConfig(applicationContext.config)
        restConfig.injectInto.value.each { type ->
            applicationContext.getArtefacts(type).each { artifactClass ->
                addDynamicMethods(restConfig, artifactClass)
            }
        }
    }

    private addDynamicMethods(RestConfig restConfig, artifactClass) {

        artifactClass.metaClass.withAsyncHttp =
            RestClientFixture.&withClient.curry(restConfig, AsyncHTTPBuilder, artifactClass)

        artifactClass.metaClass.withHttp =
            RestClientFixture.&withClient.curry(restConfig, HTTPBuilder, artifactClass)

        artifactClass.metaClass.withRest =
            RestClientFixture.&withClient.curry(restConfig, RESTClient, artifactClass)
    }

}
