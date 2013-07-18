/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.rest.ssl

import grails.test.GrailsUnitTestCase

/**
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

  void testGetKeyStoreFromConf() {
    def config = mockConfig(KEYSTORE_CONFIG)

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def keyStoreModel = sksf.getKeyStoreModel(config)
    assert keyStoreModel.keystore, "KeyStore expected"
  }

  void testGetKeyStoreFromDefault() {

    def config = mockConfig('')

    SimpleKeyStoreFactory.metaClass.getDefaultKeyStoreHome = {
      'test/resources/certs'
    }

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()

    def keyStoreModel = sksf.getKeyStoreModel(config)
    assert keyStoreModel.keystore, "KeyStore expected "
    assert keyStoreModel.path == "test/resources/certs/.keystore"
  }

  void testGetTrustStoreFromConf() {
    def config = mockConfig(TRUSTSTORE_CONFIG)

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def trustStoreModel = sksf.getTrustStoreModel(config)
    assert trustStoreModel.keystore, "KeyStore expected"
  }

  void testGetTrustStoreFromDefault() {
    def config = mockConfig('')

    SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
    def trustStoreModel = sksf.getTrustStoreModel(config)
    assert trustStoreModel.keystore, "KeyStore expected"
    assert trustStoreModel.path == '/truststore.jks'
  }
}
