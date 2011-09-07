package org.grails.plugins.rest

import groovyx.net.http.HTTPBuilder
import org.grails.plugins.rest.RestConfig

/** */
interface HTTPBuilderCustomization {

  /**
   * Through this main method, and normaly using a  {@link org.grails.plugins.rest.ssl.KeyStoreFactory}    and a <code>org.apache.http.conn.ssl.SSLSocketFactory</code>,
   * it should register an SSL Scheme containing a java.security.KeyStore, loaded with either a <i>keystore</i> and/or a
   * <i>truststore</i>, into the Builder.Client's Connection Manager.
   * @param config Configuration holder that might be used to register an SSL Scheme in the given client's Connection Manager.
   */
  HTTPBuilder apply(RestConfig config, HTTPBuilder client)

}

