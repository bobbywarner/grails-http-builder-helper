/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.rest.ssl

import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 *
 * @author berngp
 */
class SimpleKeyStoreFactoryTests extends GrailsUnitTestCase {

  String KEYSTORE_CONFIG = '''
    https.keystore.path='test/resources/certs/keystore.jks'
    https.keystore.pass='test1234'
    '''

  String TRUSTSTORE_CONFIG = '''
    https.truststore.path='test/resources/certs/truststore.jks'
    https.truststore.pass='test1234'
    '''


  protected void setUp() {
    super.setUp()

  }

  protected void tearDown() {
    super.tearDown()
  }

  void testGetKeyStoreFromConf() {
    mockConfig KEYSTORE_CONFIG

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def keyStoreModel = sksf.getKeyStoreModel(ConfigurationHolder.config)
    assert keyStoreModel.keystore, "KeyStore expected"
  }

  void testGetKeyStoreFromDefault() {

    mockConfig ''

    SimpleKeyStoreFactory.metaClass.getDefaultKeyStoreHome = {
      'test/resources/certs'
    }

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()

    def keyStoreModel = sksf.getKeyStoreModel(ConfigurationHolder.config)
    assert keyStoreModel.keystore, "KeyStore expected "
    assert keyStoreModel.path == "test/resources/certs/.keystore"
  }

  void testGetTrustStoreFromConf() {
    mockConfig TRUSTSTORE_CONFIG

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def trustStoreModel = sksf.getTrustStoreModel(ConfigurationHolder.config)
    assert trustStoreModel.keystore, "KeyStore expected"
  }

  void testGetTrustStoreFromDefault() {
    mockConfig ''

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def trustStoreModel = sksf.getTrustStoreModel(ConfigurationHolder.config)
    assert trustStoreModel.keystore, "KeyStore expected"
    assert trustStoreModel.path == '/truststore.jks'
  }

}

