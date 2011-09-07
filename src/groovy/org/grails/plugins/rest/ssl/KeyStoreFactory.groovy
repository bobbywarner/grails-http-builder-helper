package org.grails.plugins.rest.ssl

/**
 * A KeyStore Factory provides a mechanism to create and load a java.security.KeyStore,
 * <i>keystore</i> and/or <i>truststore</i>.
 * @author berngp
 */
protected interface KeyStoreFactory {
    /** */
    KeyStoreModel getKeyStoreModel(String fullResourcePath, String password)
    /** */
    KeyStoreModel getTrustStoreModel(String fullResourcePath, String password)

    KeyStoreCommand newKeyStoreCommand(Map<String, String> args)

    KeyStoreCommand newTrustStoreCommand(Map<String, String> args)


}