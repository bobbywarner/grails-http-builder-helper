package grails.plugin.httpbuilderhelper.ssl

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class SimpleKeyStoreFactoryTests extends Specification {

    void testGetKeyStoreFromConf() {
        given:
        config.https.keystore.path = 'src/test/resources/certs/keystore.jks'
        config.https.keystore.pass = 'test1234'

        when:
        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def keyStoreModel = sksf.getKeyStoreModel()

        then:
        keyStoreModel.keystore != null
    }

    void testGetKeyStoreFromDefault() {
        given:
        SimpleKeyStoreFactory.metaClass.getDefaultKeyStoreHome = {
            'src/test/resources/certs'
        }
        config.https.keystore.path = ''
        config.https.keystore.pass = ''

        when:
        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def keyStoreModel = sksf.getKeyStoreModel()

        then:
        keyStoreModel.keystore != null
        keyStoreModel.path == "src/test/resources/certs/.keystore"
    }

    void testGetTrustStoreFromConf() {
        given:
        config.https.truststore.path = 'src/test/resources/certs/truststore.jks'
        config.https.truststore.pass = 'test1234'

        when:
        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def trustStoreModel = sksf.getTrustStoreModel()

        then:
        trustStoreModel.keystore != null
    }

    void testGetTrustStoreFromDefault() {
        given:
        SimpleKeyStoreFactory.metaClass.getDefaultTrustStoreHome = {
            'src/test/resources'
        }
        config.https.truststore.path = ''
        config.https.truststore.pass = ''

        when:
        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def trustStoreModel = sksf.getTrustStoreModel()

        then:
        trustStoreModel.keystore != null
        trustStoreModel.path == 'src/test/resources/truststore.jks'
    }
}
