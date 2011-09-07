package org.grails.plugins.rest.ssl

import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

protected class SimpleKeyStoreFactory implements KeyStoreFactory {

    private static final Logger log = LoggerFactory.getLogger(SimpleKeyStoreFactory);
    /** Default Key Store file.         */
    private static final String DEFAULT_KEYSTORE = ".keystore"
    /** Default Trust Store Classpath file.        */
    private static final String DEFAULT_CLASSPATH_TRUSTSTORE = "/truststore.jks"
    /** Set of common default Key/Trust Store files passwords.         */
    private static final Set<String> COMMON_PASSWORDS = (['', 'changeit', 'changeme'] as LinkedHashSet).asImmutable()

    /**
     * Used to specify a getter for the default directory hosting the KeyStore. Currently it is set to the <i>User's Home</i>
     * as seen by System.getProperty('user.home').
     */
    String getDefaultKeyStoreHome() { System.getProperty('user.home') }

    /**
     * Looks for the <i>Key Store</i> file, loads it, and generates a {@link KeyStoreModel}.
     * The <code>config:ConfigObject</code> may specify a <b>path</b> to the <i>Key Store</i> through <i>https.keystore.path</i>,
     * if none is specified it will use a path defined as"
     * <br/>
     * <i>Default Key Store Home ( see  {@link #getDefaultKeyStoreHome()}  )</i><b>/</b><i>( value of  {@link #DEFAULT_KEYSTORE} )</i>"
     * <br/>
     * In the same manner a password for such <i>Key Store</i> might be specified through <i>https.keystore.pass</i>,
     * If none we will try to guess using common default keystore passwords as defined by the  {@link #COMMON_PASSWORDS}  set.
     */
    KeyStoreModel getKeyStoreModel(String fullResourcePath, String password) {
        loadKeyStoreModel(fullResourcePath ?: "${this.defaultKeyStoreHome}/${DEFAULT_KEYSTORE}", password)
    }

    /**
     * Looks for the <i>Trust Store</i> file, loads it, and generates a Map from it as specified in the   {@link #getKeyStoreModel}   method.
     * The <code>config:ConfigObject</code> may specify a path for the Trust Store through <i>https.truststore.path</i>, if none is specified it
     * will use   {@link #DEFAULT_CLASSPATH_TRUSTSTORE}  . In the same manner a password for such Trust Store might be specified through <i>https.truststore.pass</i>,
     * If none we will try to guess using common default keystore passwords.
     */
    KeyStoreModel getTrustStoreModel(String fullResourcePath, String password) {
        loadKeyStoreModel(fullResourcePath ?: DEFAULT_CLASSPATH_TRUSTSTORE, password)
    }

    KeyStoreCommand newKeyStoreCommand(Map<String, String> args){
        new AbstractKeyStoreCommand(args) {
            KeyStoreModel execute(){
                SimpleKeyStoreFactory.this.getKeyStoreModel(resourcePath, password)

            }
        }
    }

    KeyStoreCommand newTrustStoreCommand(Map<String, String> args){
         new AbstractKeyStoreCommand(args) {
            KeyStoreModel execute(){
                SimpleKeyStoreFactory.this.getTrustStoreModel(resourcePath, password)
            }
        }
    }

    private KeyStoreModel loadKeyStoreModel(String resourceFullPath, String knownPassword) {
        final Resource resource = getResourceIfAvailable(resourceFullPath)
        final KeyStoreModel keyStoreModel
        if (resource) {
            keyStoreModel = loadKeyStore(resource, knownPassword)
        } else {
            keyStoreModel = null
        }
        keyStoreModel
    }

    private Resource getResourceIfAvailable(String resourceFullPath) {
        Resource resource = null
        if (resourceFullPath) {
            resource = getResourceFromClassPath(resourceFullPath)
            if (!resource) {
                resource = getResourceFromFile(resourceFullPath)
            }
        }
        resource
    }

    private KeyStoreModel loadKeyStore(Resource resource, String knownPassword = null) {
        KeyStore.getInstance(KeyStore.defaultType)
        //and define a set passwords we will use to open such store, if non are defined we will use a default set
        Set<String> keyStorePasswords = knownPassword ? [knownPassword] : COMMON_PASSWORDS

        KeyStore keyStore = KeyStore.getInstance(KeyStore.defaultType)

        String correctPassword = null
        for (String password: keyStorePasswords) {
            try {
                keyStore.load(resource.inputStream, password.toCharArray())
                correctPassword = password;
                break;
            } catch (CertificateException e) {
                log.debug e.message, e
            } catch (NoSuchAlgorithmException e) {
                log.debug e.message, e
            } catch (FileNotFoundException e) {
                log.debug e.message, e
            } catch (IOException e) {
                log.debug e.message, e
            }
        }
        correctPassword ? new KeyStoreModel(keyStore: keyStore, password: correctPassword) : null
    }

    /**
     * Loads a resource from the <i>File System</i> given the full path to that resource, including the file's name and extension.
     * @param resourceFullPath full path to that resource
     * @return The {@link org.springframework.core.io.Resource Resource} or null if the resource was not found or a
     */
    private Resource getResourceFromFile(String resourceFullPath) {
        getResource(resourceFullPath, FileSystemResource)
    }

    /**
     * Loads a resource from the <i>Class Path</i> given the full path to that resource, including the file's name and extension.
     * @param resourceFullPath full path to that resource
     * @return The {@link org.springframework.core.io.Resource Resource} or null if the resource was not found or a
     */
    private Resource getResourceFromClassPath(String resourceFullPath) {
        getResource(resourceFullPath, ClassPathResource)
    }

    private Resource getResource(String resourceFullPath, Class strategy, String qualifier = "") {
        Resource resource = strategy.newInstance([resourceFullPath]) as Resource
        try {
            resource.URL ? resource : null
        } catch (anyException) {
            log.warn "Unable to load ${resourceFullPath} through the ${qualifier ?: strategy}.", anyException
        }
        null
    }
}

