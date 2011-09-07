package org.grails.plugins.rest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * User: berngp
 * Date: 9/5/11
 * Time: 8:42 PM
 */
final class RestConfig {

    /** The URL requested doesn't need to match the URL in the Certificate.*/
    static final String CERT_HOSTNAME_VERIFIER_ALLOW_ALL = 'ALLOW_ALL'
    /** The URL requested needs to match the URL in the Certificate.*/
    static final String CERT_HOSTNAME_VERIFIER_STRICT = 'STRICT'
    /** The URL requested must be in the same domain as the one in the Certificate*/
    static final String CERT_HOSTNAME_VERIFIER_BROWSER_COMPATIBLE = 'BROWSER_COMPATIBLE'
    /** Strategies to verify Certificates on remote servers according to the trust-stores.*/
    static final Set<String> CERT_HOSTNAME_VERIFIERS = [
            CERT_HOSTNAME_VERIFIER_ALLOW_ALL, CERT_HOSTNAME_VERIFIER_STRICT, CERT_HOSTNAME_VERIFIER_BROWSER_COMPATIBLE
    ].asImmutable()

    /** */
    static final String HTTPS = "https"
    /** */
    static final int SSL_DEFAULT_PORT = 443

    private final ConfigObject config


    protected RestConfig(ConfigObject config) {
        this.config = config
    }

    static RestConfig wrap(ConfigObject config) {
        assert config, '''config:ConfigObject can't be null.'''
        new RestConfig(config)
    }

    ConfigurationEntry<List<String>> getInjectInto() {
        ConfigurationEntry.instance(config, 'injectInto', [ defaultValue: ["Controller", "Service"] ])
    }

    ConfigurationEntry<String> getHostNameVerifierStrategy() {
        ConfigurationEntry.instance(config, 'https.cert.hostnameVerifier',
                [ acceptedValues: CERT_HOSTNAME_VERIFIER_BROWSER_COMPATIBLE]) {
            it ? it.toUpperCase() : ''
        }
    }

    ConfigurationEntry<String> getPathToKeyStore() {
        ConfigurationEntry.instance(config, 'https.keystore.path')
    }

    ConfigurationEntry<String> getKeyStorePassword() {
        ConfigurationEntry.instance(config, 'https.keystore.password')
    }

    ConfigurationEntry<String> getPathToTrustStore() {
        ConfigurationEntry.instance(config, 'https.truststore.path')
    }

    ConfigurationEntry<String> getTrustStorePassword() {
        ConfigurationEntry.instance(config, 'https.truststore.password')
    }

    boolean isSslEnabled() {
        pathToTrustStore.value || pathToKeyStore.value
    }
}

