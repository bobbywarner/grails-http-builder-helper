package org.grails.plugins.rest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * User: berngp
 * Date: 9/6/11
 * Time: 6:36 PM
 */
protected class ConfigurationEntry<T> {

    protected final static Log LOG = LogFactory.getLog(ConfigurationEntry)

    final ConfigObject config

    final String internalPath

    final T defaultValue

    final Set<T> acceptedValues

    protected final Closure postProcessor

    private ConfigurationEntry(ConfigObject config,
                               String internalPath,
                               T defaultValue,
                               Set<T> acceptedValues,
                               Closure<T> postProcessor) {
        assert config, 'Please define a valid config:RestConfig reference!'
        this.config = config
        assert internalPath, 'Please define an internapath. e.g such as "path.to.conf" '
        this.internalPath = internalPath
        //optionals
        this.defaultValue = defaultValue
        this.acceptedValues = acceptedValues ? acceptedValues.asImmutable() : Collections.EMPTY_SET
        this.postProcessor = postProcessor
    }

    static protected ConfigurationEntry<T> instance(ConfigObject config,
                                                  String internalPath,
                                                  Map args = null,
                                                  Closure postProcessor) {
        new ConfigurationEntry<T>(config, internalPath,
                args?.defaultValue, args?.acceptedValues, postProcessor)
    }


    static protected ConfigurationEntry<T> instance(ConfigObject config, String internalPath, Map args) {
        new ConfigurationEntry<T>(config, internalPath, args?.defaultValue, args?.acceptedValues, null)
    }

    static protected ConfigurationEntry<T> instance(ConfigObject config, String internalPath) {
        new ConfigurationEntry<T>(config, internalPath, null, null, null)
    }

    final String getPath() { "grails.rest.$internalPath" }

    final T getValue() {
        T value = null
        def node = this.config
        final List<String> pathTokens = path.tokenize('.').reverse()
        while (node && node instanceof Map && pathTokens.size()) {
            node = node[pathTokens.pop()]
        }
        if (node != null && pathTokens.empty) {
            value = node as T
            LOG.debug "key '$path' resolved to value [$value]"
        }
        if (value == null && defaultValue != null) {
            value = defaultValue
        }
        if (postProcessor) {
            value = postProcessor.call(value) as T
        }
        value
    }

}
