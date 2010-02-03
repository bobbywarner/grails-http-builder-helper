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
import java.lang.reflect.InvocationTargetException

/**
 * @author Andres.Almiray
 */
class RestGrailsPlugin {
    // the plugin version
    def version = "0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def author = "Andres Almiray"
    def authorEmail = "aalmiray@users.sourceforge.net"
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
      def config = ConfigurationHolder.config
      def application = ApplicationHolder.application
      def types = config.grails?.rest?.injectInto ?: ["Controller", "Service"]
      types.each { type ->
         application.getArtefacts(type).each{ klass ->
            addDynamicMethods(klass)
         }
      }
   }

   private addDynamicMethods(klass) {
      klass.metaClass.withAsyncHttp = withClient.curry(AsyncHTTPBuilder, klass)
      klass.metaClass.withHttp = withClient.curry(HTTPBuilder, klass)
      klass.metaClass.withRest = withClient.curry(RESTClient, klass)
   }

   // ======================================================

   private withClient = { Class klass, Object target, Map params, Closure closure ->
      def client = null
      if(params.id) {
         String id = params.remove("id").toString()
         if(target.metaClass.hasProperty(target, id)) {
            client = target."$id"
         } else {
            client = makeClient(klass, params) 
            target.metaClass."$id" = client
         }
      } else {
        client = makeClient(klass, params) 
      }

      if(params.containsKey("proxy")) {
         Map proxyArgs = [scheme: "http", port: 80] + params.remove("proxy")
         if(!proxyArgs.host) throw new IllegalArgumentException("proxy.host cannot be null!")
         client.setProxy(proxyArgs.host, proxyArgs.port as int, proxyArgs.scheme)
      }

      if(closure) {
         closure.delegate = client
         closure.resolveStrategy = Closure.DELEGATE_FIRST
         closure()
      }
   }

   private makeClient(Class klass, Map params) {
      if(klass == AsyncHTTPBuilder) {
         try {
            Map args = [:]
            ["threadPool", "poolSize", "uri", "contentType", "timeout"].each { arg ->
               if(params[(arg)] != null) args[(arg)] = params[(arg)]
            }
            return klass.newInstance(args)
         } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to create async http client reason: $e", e)
         } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to create async http client reason: $e", e)
         }
      }
      try {
         def client =  klass.newInstance()
         if(params.uri) client.uri = params.remove("uri")
         if(params.contentType) client.contentType = params.remove("contentType")
         return client
      } catch (IllegalArgumentException e) {
         throw new RuntimeException("Failed to create ${(klass == HTTPBuilder? 'http' : 'rest')} client reason: $e", e)
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Failed to create ${(klass == HTTPBuilder? 'http' : 'rest')} client reason: $e", e)
      }
   }
}
