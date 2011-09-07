package org.grails.plugins.rest.ssl

/**
 * User: berngp
 * Date: 9/6/11
 * Time: 4:31 PM
 */
protected interface KeyStoreCommand {
    String password
    String resourcePath

    KeyStoreModel execute()
}
