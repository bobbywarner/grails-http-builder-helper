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
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.conn.ProxySelectorRoutePlanner

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.lang.reflect.InvocationTargetException

import org.grails.plugins.rest.ssl.HTTPBuilderSSLHelper
import org.grails.plugins.rest.ssl.HTTPBuilderSSLConstants
import org.grails.plugins.rest.ssl.SimpleHTTPBuilderSSLHelper

/**
 * @author Andres.Almiray
 */
class RestGrailsPlugin {
	// the plugin version
	def version = "0.6.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.2.0 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
		"grails-app/views/error.gsp"
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

	/**
	 */
	HTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

	def doWithDynamicMethods = { ctx -> processArtifacts() }

	def onChange = { event -> processArtifacts() }

	def onConfigChange = { event -> processArtifacts() }

	private processArtifacts() {
		def config = ConfigurationHolder.config
		def application = ApplicationHolder.application
		def types = config.grails?.rest?.injectInto ?: ["Controller", "Service"]
		types.each { type ->
			application.getArtefacts(type).each { klass -> addDynamicMethods(klass) }
		}
	}

	private addDynamicMethods(klass) {
		klass.metaClass.withAsyncHttp = withBuilder.curry(AsyncHTTPBuilder, klass)
		klass.metaClass.withHttp = withBuilder.curry(HTTPBuilder, klass)
		klass.metaClass.withRest = withBuilder.curry(RESTClient, klass)
	}

	// ======================================================

	private withBuilder = { Class klass, Object target, Map params, Closure closure ->
		def builder = null
		if (params.id) {
			String id = params.remove("id").toString()
			if (target.metaClass.hasProperty(target, id)) {
				builder = target.metaClass.getProperty(target, id)
			} else {
				builder = makeBuilder(klass, params)
				target.metaClass."$id" = builder
			}
		} else {
			builder = makeBuilder(klass, params)
		}

		setRoutePlanner(builder)

		if (closure) {
			closure.delegate = builder
			closure.resolveStrategy = Closure.DELEGATE_FIRST
			closure()
		}
	}

	private makeBuilder(Class klass, Map params) {
		def builder
		if (klass == AsyncHTTPBuilder) {
			builder = makeAsyncBuilder(klass, params)

		} else {
			builder = makeSyncBuilder(klass, params)
		}

		if (HTTPBuilderSSLConstants.HTTPS == builder.uri.toURL().protocol) {
			addSSLSupport(builder)
		}

		return builder
	}

	private makeAsyncBuilder(Class klass, Map params){
		def builder
		try {
			Map args = [:]
			[ "threadPool", "poolSize", "uri", "contentType", "timeout" ].each { arg ->
				if (params[(arg)] != null) args[(arg)] = params[(arg)]
			}
			builder = klass.newInstance(args)

		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to create async http builder reason: $e", e)
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to create async http builder reason: $e", e)
		}
		return builder
	}

	private makeSyncBuilder(Class klass, Map params){
		def builder
		try {
			builder = klass.newInstance()
			if (params.uri) builder.uri = params.remove("uri")
			if (params.contentType) builder.contentType = params.remove("contentType")

		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to create ${(klass == HTTPBuilder ? 'http' : 'rest')} builder reason: $e", e)
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to create ${(klass == HTTPBuilder ? 'http' : 'rest')} builder reason: $e", e)
		}
		return builder
	}

	private addSSLSupport(builder){
		try {
			sslHelper.addSSLSupport(ConfigurationHolder.config?.rest, builder)

		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to add ssl support to ${(klass == HTTPBuilder ? 'https' : 'rest')} builder reason: $e", e)
		} catch (IllegalStateException e) {
			throw new RuntimeException("Failed to add ssl support to ${(klass == HTTPBuilder ? 'https' : 'rest')} builder reason: $e", e)
		}
	}
	
	private setRoutePlanner(builder){
		builder.client.routePlanner = new ProxySelectorRoutePlanner(
			builder.client.connectionManager.schemeRegistry,
    		ProxySelector.default
		)
	}
}
