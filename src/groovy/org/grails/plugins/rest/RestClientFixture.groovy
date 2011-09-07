package org.grails.plugins.rest

import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder
import java.lang.reflect.InvocationTargetException
import org.grails.plugins.rest.ssl.SSLCustomization

/**
 * User: berngp
 * Date: 6/12/11
 * Time: 3:43 AM
 */
class RestClientFixture {

    static  RestConfig getConfig(ConfigObject config){
         RestConfig.wrap(config)
    }

    static def withClient(RestConfig config, Class builderClass, Object target, Map params, Closure closure) {
        HTTPBuilder client
        if (params.id) {
            String id = params.remove("id").toString()
            if (target.metaClass.hasProperty(target, id)) {
                client = target.metaClass.getProperty(target, id) as HTTPBuilder
            } else {
                client = makeClient(config, builderClass, params)
                target.metaClass.setProperty(target, id, client)
            }
        } else {
            client = makeClient(config, builderClass, params)
        }

        if (params.containsKey("proxy")) {
            Map proxyArgs = [scheme: "http", port: 80] + params.remove("proxy")
            if (!proxyArgs.host) throw new IllegalArgumentException("proxy.host cannot be null!")
            client.setProxy(proxyArgs.host, proxyArgs.port as int, proxyArgs.scheme)
        }

        if (closure) {
            closure.delegate = client
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()
        }
    }

    static HTTPBuilder makeClient(RestConfig config, Class builderClass, Map params) {

        HTTPBuilder client
        if (builderClass == AsyncHTTPBuilder) {
            try {
                Map args = [:]
                ["threadPool", "poolSize", "uri", "contentType", "timeout"].each { arg ->
                    if (params[(arg)] != null) args[(arg)] = params[(arg)]
                }
                client = builderClass.newInstance(args) as HTTPBuilder
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Failed to create async http client reason: $e", e)
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to create async http client reason: $e", e)
            }

        } else {
            try {
                client = builderClass.newInstance() as HTTPBuilder
                if (params.uri) client.uri = params.remove("uri")
                if (params.contentType) client.contentType = params.remove("contentType")

            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Failed to create ${(builderClass == HTTPBuilder ? 'http' : 'rest')} client reason: $e", e)
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to create ${(builderClass == HTTPBuilder ? 'http' : 'rest')} client reason: $e", e)
            }
        }

        RestClientFixture.configureSSL(config, client)
        client
    }

    static HTTPBuilder configureSSL(RestConfig config, HTTPBuilder builder) {
        SSLCustomization sslCustomization = new SSLCustomization()
        try {
            sslCustomization.apply(config, builder);
        } catch ( anyException ) {
            throw new IllegalStateException(
                    "Failed to add ssl support to ${(builder.class == HTTPBuilder ? 'http' : 'rest')} client reason: ${anyException.message}",
                    anyException)
        }
        builder
    }
}
