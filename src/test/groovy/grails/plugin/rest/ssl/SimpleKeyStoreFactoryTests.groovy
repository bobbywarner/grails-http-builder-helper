package grails.plugin.rest.ssl

class SimpleKeyStoreFactoryTests extends GroovyTestCase {

    ConfigObject keystoreConfig = new ConfigObject()
    ConfigObject truststoreConfig = new ConfigObject()

    void testGetKeyStoreFromConf() {
        keystoreConfig.put('https.keystore.path', 'classpath:certs/keystore.jks')
        keystoreConfig.put('https.keystore.pass', 'test1234')

        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def keyStoreModel = sksf.getKeyStoreModel(keystoreConfig)
        assert keyStoreModel.keystore, "KeyStore expected"
    }

    void testGetKeyStoreFromDefault() {
        SimpleKeyStoreFactory.metaClass.getDefaultKeyStoreHome = {
            'test/resources/certs'
        }

        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()

        def keyStoreModel = sksf.getKeyStoreModel(new ConfigObject())
        assert keyStoreModel.keystore, "KeyStore expected "
        assert keyStoreModel.path == "classpath:certs/.keystore"
    }

    void testGetTrustStoreFromConf() {
        truststoreConfig.put('https.truststore.path', 'classpath:certs/truststore.jks')
        truststoreConfig.put('https.truststore.pass', 'test1234')

        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def trustStoreModel = sksf.getTrustStoreModel(truststoreConfig)
        assert trustStoreModel.keystore, "KeyStore expected"
    }

    void testGetTrustStoreFromDefault() {
        SimpleKeyStoreFactory sksf = new SimpleKeyStoreFactory()
        def trustStoreModel = sksf.getTrustStoreModel(new ConfigObject())
        assert trustStoreModel.keystore, "KeyStore expected"
        assert trustStoreModel.path == 'classpath:certs/truststore.jks'
    }
}
