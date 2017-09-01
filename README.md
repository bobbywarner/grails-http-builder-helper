Grails HTTP Builder Helper
==========================

This plugin used to be called the REST plugin in Grails 1.x & 2.x, but for Grails 3.x it has been renamed to `httpbuilder-helper`.  The plugin enables the usage of HTTPBuilder in a Grails application.

# Description

The REST plugin enables the usage of HTTPBuilder on a Grails application.

## Usage

The plugins will inject the following dynamic methods:

* `withHttp(Map params, Closure stmts)` - executes stmts using a `HTTPBuilder`
* `withAsyncHttp(Map params, Closure stmts)` - executes stmts using an `AsyncHTTPBuilder`
* `withRest(Map params, Closure stmts)` - executes stmts using a `RESTClient`

## Examples

Taken from HttpBuilder's Simplified GET Request

```groovy
withHttp(uri: "http://www.google.com") {
   def html = get(path : '/search', query : [q:'Groovy'])
   assert html.HEAD.size() == 1
   assert html.BODY.size() == 1
}
```

Notice that you can call `HTTPBuilder`'s methods inside `stmts`, the current `HTTPBuilder` is set as the closure's delegate. The same holds true for the other dynamic methods.
`AsyncHTTPBuilder`

```groovy
import static groovyx.net.http.ContentType.HTML
withAsyncHttp(poolSize : 4, uri : "http://hc.apache.org", contentType : HTML) {
   def result = get(path:'/') { resp, html ->
      println ' got async response!'
      return html
   }
   assert result instanceof java.util.concurrent.Future

   while (! result.done) {
      println 'waiting...'
      Thread.sleep(2000)
   }

   /* The Future instance contains whatever is returned from the response
      closure above; in this case the parsed HTML data: */
   def html = result.get()
   assert html instanceof groovy.util.slurpersupport.GPathResult
}
```

All dynamic methods will create a new http client when invoked unless you define an id: attribute. When this attribute is supplied the client will be stored as a property on the instance's metaClass. You will be able to access it via regular property access or using the id: again.

```groovy
class FooController {
  def loginAction = {
    withRest(id: "twitter", uri: "http://twitter.com/statuses/") {
      auth.basic model.username, model.password
    }
  }
  def queryAction = {
    withRest(id: "twitter") {
       def response = get(path: "followers.json")
       // …
    }
    /* alternatively
      def response twitter.get(path: "followers.json")
    */
  }
}
```

# Configuration

## Dynamic method injection

Dynamic methods will be added to controllers and services by default. You can change this setting by adding a configuration flag `application.yml` or `application.groovy`:

```groovy
grails.rest.injectInto = ["Controller", "Service", "Routes"]
```

## Proxy settings

You can apply proxy settings by calling `setProxy(String host, int port, String scheme)` on the client/builders at any time. You can also take advantage of the `proxy:` shortcut

```groovy
withHttp(uri: "http://google.com", proxy: [host: "myproxy.acme.com", port: 8080, scheme: "http"])
```

This shortcut has the following defaults

```
port: = 80
scheme: = http
```

Meaning most of the times you'd only need to define a value for `host:`

## SSL Key-Store and Trust-Store support

If you are connecting to a server through HTTPS you might need to add a Key and or a Trust Store to the underlying SSL Socket Factory. Some examples are…

The service you are connecting to requires some sort of SSL Cert authentication,
You want to make sure that the server you are connecting to provides a specific certificate.
You do not mind that the certificate that the server provides doesn't match the Domain Name that the server has.
Note, this last option will normally apply to development and test environments.

### So how can I add the Key and/or Trust Stores to the underlying SSL Socket Factory?

The client will try to add a Key and a Trust Store if the URL of the host starts with `https`. By default it will attempt to locate a Key Store through the File System's path `$HOME/.keystore` e.g `/home/berngp/.keystore` and a Trust Store through the JVM Classpath `./truststore.jks`, you can override this by specifying a `rest.https.keystore.path` and/or `rest.https.truststore.path` configuration entries in the `application.yml` or `application.groovy` file.

Also by default it will try to open the stores using the following passwords '', 'changeit', 'changeme' but you can set a specific password through the `rest.https.keystore.pass` and `rest.https.truststore.pass` configuration entries. If for some reason it is unable to setup the underlying SSL Socket Factory it will fail silently unless the `rest.https.sslSocketFactory.enforce` configuration entry is set to `true`.


### Specifying a Hostname Verification strategy for the Trust Store.

You can set three different Hostname Verification strategies through the `rest.https.cert.hostnameVerifier` configuration entry.

`ALLOW_ALL`: The URL requested doesn't need to match the URL in the Certificate.
`STRICT`: The URL requested needs to match the URL in the Certificate.
`BROWSER_COMPATIBLE`: The URL requested must be in the same domain as the one in the Certificate. It accepts wildcards.
Example of a specific setup

```groovy
/** SSL truststore configuration key */
rest.https.truststore.path = 'resources/certs/truststore.jks'
/** SSL keystore configuration key */
rest.https.keystore.path='resources/certs/keystore.jks'
/** SSL keystore password configuration key */
rest.https.keystore.pass='changeme'
/** Certificate Hostname Verifier configuration key */
rest.https.cert.hostnameVerifier = 'BROWSER_COMPATIBLE'
/** Enforce SSL Socket Factory */
rest.https.sslSocketFactory.enforce = true
```

### Generating a Key Store

Generating a Java Key Store is outside the scope of this guide but you can find some useful information through the following links.

* Official JDK Guide - http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html

* Importing a Private Key into a KeyStore (JDK 1.6 and above) - http://cunning.sharp.fm/2008/06/importing_private_keys_into_a.html
* KeyMan - An non JDK Key Tool by IBM - http://www.alphaworks.ibm.com/tech/keyman
