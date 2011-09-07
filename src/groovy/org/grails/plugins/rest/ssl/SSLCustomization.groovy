package org.grails.plugins.rest.ssl

import groovyx.net.http.HTTPBuilder
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.grails.plugins.rest.RestConfig
import org.grails.plugins.rest.HTTPBuilderCustomization

/**
 * Applies any SSL customizations as hinted by the given {@link ConfigObject} defined through the {@link SSLCustomization#apply}
 * method. and a <code>org.apache.http.conn.ssl.SSLSocketFactory</code>
 * it registers an SSL Scheme containing a java.security.KeyStore, loaded with either a <i>keystore</i> and/or a
 * <i>truststore</i>, into the Builder.Client's Connection Manager.
 * By default the it will use a  {@link SimpleKeyStoreFactory}  .
 * Please review the   {@link SimpleKeyStoreFactory}   documentation to be aware of its configuration
 * keys hosted inside the <i>ConfigObject</i>.
 * @see org.apache.http.conn.ssl.SSLSocketFactory
 * @see org.apache.http.conn.scheme.Scheme
 * @see org.grails.plugins.rest.ssl.SimpleKeyStoreFactory
 * @author berngp
 * @since 0.6
 */
class SSLCustomization implements HTTPBuilderCustomization {

    /**
     * Defines the KeyStoreFactory used if connecting to an endpoints behind SSL.
     * @todo Make sure that there exists a clear mechanism to inject the restPluginKeyStoreFactory through the Spring Context
     */
    KeyStoreFactory restPluginKeyStoreFactory = new SimpleKeyStoreFactory()

    /**
     * If a <i>Key Store</i> and/or a <i>Trust Store</i> are defined it creates a <code>org.apache.http.conn.ssl.SSLSocketFactory</code> attaching
     * it to the given <code>builder.client.connectionManager</code>.
     * The <code>config:ConfigObject</code> may specify the following value(s) among others used by the <i>KeyStoreFactory</i>:
     *   <ul>
     *      <li>https.cert.hostnameVerifier:  As defined by <code>org.apache.http.conn.ssl.SSLSocketFactory</code>, eg.
     *             <code>https.cert.hostnameVerifier=allow_all</code>. If none is specified the default SSLSocketFactory's strategy will be used.</li>
     *   </ul>
     * @see org.apache.http.conn.ssl.SSLSocketFactory
     * @see org.apache.http.conn.scheme.Scheme
     * @see org.grails.plugins.rest.ssl.SimpleKeyStoreFactory
     */
    HTTPBuilder apply(RestConfig config, HTTPBuilder builder) {
        if (config.sslEnabled && RestConfig.HTTPS == (builder.uri as String).toURL().protocol) {
            addSSLSupport(config, builder)
        }
        builder
    }

    protected final HTTPBuilder addSSLSupport(RestConfig restHttpsConfig, HTTPBuilder builder) {
        assert builder, '''builder:HTTPBuilder can't be null.'''

        KeyStoreModel keyStoreModel = getKeyStoreModel(restHttpsConfig)
        KeyStoreModel trustStoreModel = getTrustStoreModel(restHttpsConfig)

        try {
            SSLSocketFactory sslSocketFactory = getSSLSocketFactory(keyStoreModel, trustStoreModel)
            if (sslSocketFactory) {
                throw new IllegalStateException("Unable to load a SSL Socket Factory!")
            }
            if (trustStoreModel) {
                sslSocketFactory = configureTrustStoreInFactory(restHttpsConfig, sslSocketFactory)
            }

            builder = embedFactoryIntoBuilder(sslSocketFactory, builder)

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e)

        } catch (KeyManagementException e) {
            throw new IllegalStateException(e)

        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(e)

        } catch (UnrecoverableKeyException e) {
            throw new IllegalArgumentException(e)
        }

        builder
    }

    protected KeyStoreModel getKeyStoreModel(RestConfig restHttpsConfig) {
        restHttpsConfig.pathToKeyStore ?
        restPluginKeyStoreFactory.
        newKeyStoreCommand(
                path: restHttpsConfig.pathToKeyStore.value,
                password: restHttpsConfig.keyStorePassword.value).execute() : null
    }

    protected KeyStoreModel getTrustStoreModel(RestConfig restHttpsConfig) {
        restHttpsConfig.pathToTrustStore ?
        restPluginKeyStoreFactory.
        newTrustStoreCommand(
                path: restHttpsConfig.pathToTrustStore.value,
                password: restHttpsConfig.trustStorePassword.value).execute() : null
    }

    protected SSLSocketFactory getSSLSocketFactory(KeyStoreModel keyStoreModel, KeyStoreModel trustStoreModel) {
        SSLSocketFactory sslSocketFactory = null
        if (keyStoreModel.keyStore && trustStoreModel.keyStore) {
            sslSocketFactory = new SSLSocketFactory(
                    keyStoreModel.keyStore, keyStoreModel.password, trustStoreModel.keyStore)

        } else if (trustStoreModel.keyStore) {
            sslSocketFactory = new SSLSocketFactory(trustStoreModel.keyStore)

        } else if (keyStoreModel.keyStore) {
            sslSocketFactory = new SSLSocketFactory(keyStoreModel.keyStore, keyStoreModel.password)
        }
        sslSocketFactory
    }

    protected final SSLSocketFactory configureTrustStoreInFactory(RestConfig restHttpsConfig,
                                                                  SSLSocketFactory sslSocketFactory) {

        if (restHttpsConfig.hostNameVerifierStrategy.value) {
            switch (restHttpsConfig.hostNameVerifierStrategy.value) {
                case RestConfig.CERT_HOSTNAME_VERIFIER_STRICT:
                    sslSocketFactory.hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER
                    break;
                case RestConfig.CERT_HOSTNAME_VERIFIER_ALLOW_ALL:
                    sslSocketFactory.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
                    break;
                case RestConfig.CERT_HOSTNAME_VERIFIER_BROWSER_COMPATIBLE:
                    sslSocketFactory.hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
                    break;
                default:
                    throw new IllegalArgumentException(
                            "The value of ${restHttpsConfig.hostNameVerifierStrategy.path} " +
                                    "doesn't match any of the following ${restHttpsConfig.hostNameVerifierStrategy.values}")

            }
        } else {
            sslSocketFactory.hostnameVerifier == restHttpsConfig.defaultHostNameVerifierStrategy
        }
        sslSocketFactory
    }

    protected final HTTPBuilder embedFactoryIntoBuilder(SSLSocketFactory sslSocketFactory, HTTPBuilder builder) {
        builder.client.connectionManager.schemeRegistry.register(
                new Scheme(RestConfig.HTTPS, sslSocketFactory, RestConfig.SSL_DEFAULT_PORT))
        builder
    }

}
