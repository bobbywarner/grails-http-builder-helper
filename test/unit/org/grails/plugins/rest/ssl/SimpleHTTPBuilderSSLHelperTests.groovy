/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.rest.ssl

import grails.test.GrailsUnitTestCase
import groovyx.net.http.HTTPBuilder
import java.security.KeyStore
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 *
 * @author berngp
 */
class SimpleHTTPBuilderSSLHelperTests extends GrailsUnitTestCase {

  def knownKeyStoreModel

  def knownTrustStoreModel

  protected void setUp() {
    super.setUp()
    // Setup a Known KeyStoreModel.
    knownKeyStoreModel = [path: 'test/resources/certs/keystore.jks', password: 'test1234']

    def knownKeyStore = KeyStore.getInstance(KeyStore.defaultType)
    File knownKeyStoreFile = new File(knownKeyStoreModel.path)
    knownKeyStoreFile.withInputStream {
      knownKeyStore.load(it, knownKeyStoreModel.password.toCharArray())
    }

    //finish setting up the model for the known keystore
    knownKeyStoreModel.URL = knownKeyStoreFile.toURL().toString()
    knownKeyStoreModel.keystore = knownKeyStore

    // Setup a Known TrustStoreModel.
    knownTrustStoreModel = [path: 'test/resources/certs/truststore.jks', password: 'test1234']

    def knownTrustStore = KeyStore.getInstance(KeyStore.defaultType)
    File knownTrustStoreFile = new File(knownTrustStoreModel.path)
    knownTrustStoreFile.withInputStream {
      knownTrustStore.load(it, knownTrustStoreModel.password.toCharArray())
    }
    //finish setting up the model for the known truststore
    knownTrustStoreModel.URL = knownTrustStoreFile.toURL().toString()
    knownTrustStoreModel.keystore = knownTrustStore

  }

  protected void tearDown() {
    super.tearDown()
  }


  void testAddSSLSupportWithoutKeysForFailingCall() {
    mockConfig ''

    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: { null },
            getTrustStoreModel: {null}
    ] as KeyStoreFactory


    def http = new HTTPBuilder('https://dev.java.net')

    sslHelper.addSSLSupport(ConfigurationHolder.config, http)

    shouldFail(javax.net.ssl.SSLException) {

      http.get(path: '/', contentType: 'text/html') { resp, reader ->
        System.out << reader
      }
    }
  }


  void testAddSSLSupportWithoutKeysForSuccessfulCall() {
    mockConfig ''

    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: { null },
            getTrustStoreModel: {null}
    ] as KeyStoreFactory

    def http = new HTTPBuilder('https://twitter.com')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }

    http = new HTTPBuilder('https://www.dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }
  }

  void testEnforceSSLSocketFactoryCalltestEnforceSSLSocketFactoryCall() {
    mockConfig 'rest.https.sslSocketFactory.enforce=true'

    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: { null },
            getTrustStoreModel: {null}
    ] as KeyStoreFactory


    shouldFail(IllegalStateException) {
      def http = new HTTPBuilder('https://twitter.com')
      sslHelper.addSSLSupport(ConfigurationHolder.config.rest, http)
      http.get(path: '/', contentType: 'text/html') { resp, reader ->
        System.out << reader
      }
    }

  }

  /** */
  private callHttpsSitesAfterKeyStores = { def sslHelper ->

    //https Twitter, now that we have keys this will fail becuase http://twitter.com is not trusted.
    shouldFail(javax.net.ssl.SSLException) {
      def httpTwitter = new HTTPBuilder('https://twitter.com')
      sslHelper.addSSLSupport(ConfigurationHolder.config, httpTwitter)
      httpTwitter.get(path: '/', contentType: 'text/html') { resp, reader ->
        System.out << reader
      }
    }

    //https://dev.java.net, we expect success since we imported the Cert into the keystore.
    def httpDefJavaNet = new HTTPBuilder('https://www.dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, httpDefJavaNet)
    httpDefJavaNet.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }

  }

  /** */
  void testAddSSLSupportWithKeyStoreOnly() {

    mockConfig ''
    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()
    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              knownKeyStoreModel
            },
            getTrustStoreModel: {
              null
            }
    ] as KeyStoreFactory
    callHttpsSitesAfterKeyStores(sslHelper)
  }

  /** */
  void testAddSSLSupportWithTrustStoreOnly() {
    mockConfig ''
    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()
    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              null
            },
            getTrustStoreModel: {
              knownTrustStoreModel
            }
    ] as KeyStoreFactory
    callHttpsSitesAfterKeyStores(sslHelper)
  }

  /** */
  void testAddSSLSupportWithAllStores() {
    mockConfig ''
    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              knownKeyStoreModel
            },
            getTrustStoreModel: {
              knownTrustStoreModel
            }
    ] as KeyStoreFactory

    callHttpsSitesAfterKeyStores(sslHelper)
  }

  /** */
  void testAddSSLSupportWithStrictCert() {

    mockConfig "https.cert.hostnameVerifier='strict'"
    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              null
            },
            getTrustStoreModel: {
              knownTrustStoreModel
            }
    ] as KeyStoreFactory

    def http = new HTTPBuilder('https://dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)

    //This call should fail since the cert in https://dev.java.net belongs to https://www.dev.java.net
    shouldFail(javax.net.ssl.SSLException) {
      http.get(path: '/', contentType: 'text/html') { resp, reader ->
        System.out << reader
      }
    }

    http = new HTTPBuilder('https://www.dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }
  }

  /** */
  void testAddSSLSupportWithBrowserCompatibleCert() {

    mockConfig "https.cert.hostnameVerifier='BROWSER_COMPATIBLE'"

    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              null
            },
            getTrustStoreModel: {
              knownTrustStoreModel
            }
    ] as KeyStoreFactory

    def http = new HTTPBuilder('https://dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)

    shouldFail(javax.net.ssl.SSLException) {
      http.get(path: '/', contentType: 'text/html') { resp, reader ->
        System.out << reader
      }
    }

    http = new HTTPBuilder('https://www.dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }
  }

  /** */
  void testAddSSLSupportWithAllowAllCert() {

    mockConfig "https.cert.hostnameVerifier='allow_all'"

    SimpleHTTPBuilderSSLHelper sslHelper = new SimpleHTTPBuilderSSLHelper()

    sslHelper.restPluginKeyStoreFactory = [
            getKeyStoreModel: {
              null
            },
            getTrustStoreModel: {
              knownTrustStoreModel
            }
    ] as KeyStoreFactory

    def http = new HTTPBuilder('https://dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }

    http = new HTTPBuilder('https://www.dev.java.net')
    sslHelper.addSSLSupport(ConfigurationHolder.config, http)
    http.get(path: '/', contentType: 'text/html') { resp, reader ->
      System.out << reader
    }
  }

}

