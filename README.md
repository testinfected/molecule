[![Build Status](https://travis-ci.org/testinfected/molecule.svg?branch=master)](https://travis-ci.org/testinfected/molecule)
[![Coverage Status](https://img.shields.io/coveralls/testinfected/molecule.svg?style=flat)](https://coveralls.io/r/testinfected/molecule)
[![Maven Central](https://img.shields.io/maven-central/v/com.vtence.molecule/molecule.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.vtence.molecule/molecule)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://vtence.mit-license.org)

[![Issues In Progress](https://badge.waffle.io/testinfected/molecule.svg?label=In%20Progress&title=Started)](https://waffle.io/testinfected/molecule)

## Quick Start

```java
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        server.start((request, response) -> response.done("Hello, World"));
    }
}
```

Access your application at:

`http://localhost:8080`

## Download 

You can get the latest release version from Maven Central:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.10</version>
</dependency>
```
 
To use the default web server, you also need to add [Simple](http://www.simpleframework.org) as a dependency:

```xml
<dependency>
      <groupId>org.simpleframework</groupId>
      <artifactId>simple-http</artifactId>
      <version>6.0.1</version>
</dependency>
```

## Want to start with some code?

Try out the following examples:

* [Hello World](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/helloworld/HelloWorldExample.java)
* [Rendering HTML](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/simple/SimpleExample.java)
* [Dynamic Routes](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/routing/RoutingExample.java)
* [Static Files](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/files/StaticFilesExample.java)
* [REST](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/rest/RESTExample.java)
* [Asynchronous Processing](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/async/AsyncExample.java)
* [Cookies](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/cookies/CookiesExample.java)
* [Flash](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/flash/FlashExample.java)
* [Locale Negotiation](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/locale/LocaleNegotiationExample.java)
* [Multipart Forms](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/multipart/MultipartExample.java)
* [View Templates and Layout](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingAndLayoutExample.java)
* [HTTP Sessions](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/session/SessionExample.java)
* [Multiple Applications](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/multiapps/MultiAppsExample.java)
* [Filters](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/filtering/FilteringExample.java)
* [Creating a Custom Middleware](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/middleware/CustomMiddlewareExample.java)
* [Caching and Compression](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/performance/CachingAndCompressionExample.java)
* [SSL](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/ssl/SSLExample.java)
* [Basic Authentication](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/auth/BasicAuthExample.java)
* [A Sample Application](https://github.com/testinfected/simple-petstore/blob/master/webapp/src/main/java/org/testinfected/petstore/PetStore.java)

## Getting Started

First thing first, you need a server to run your app:

```java
WebServer server = WebServer.create();
```

This will set the default web server, which is powered by [Simple](http://www.simpleframework.org), 
to run locally on port 8080.

To start the server, give it an app:

```java
server.start((request, response) -> response.done("Hello, World!"));
```

To stop the server, call the _stop_ method:

```java
server.stop()
```

You can optionally specify the interface and port to bound to when creating the server, e.g. if you want to make your server globally available:

```java
WebServer server = WebServer.create("0.0.0.0", 8088);
```

## Asynchronous Processing

Molecule uses [Simple](http://www.simpleframework.org) as a default webserver.
Both are fully asynchronous and non-blocking. This allows the server to scale to very high loads and handle as many concurrent connections as possible, even when depending on a high latency external resource.
         
What this means is you can serve your response content from a thread separate to the original servicing thread. For instance your application 
might need to wait for some remote process that takes some time to complete, such as an HTTP or SOAP request to an external server. You can simply 
call this external resource from a different thread, and complete the response when you get the result.

To tell the server that you're ready to serve the response, call the <code>done</code> method on the response.

Look at the [Asynchronous example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/async/AsyncExample.java)
to see how to serve content from a separate thread.


## Routing

Most modern webapps have nice URLs. Simple URLs are also easier to remember and more user friendly. 

Molecule comes with a routing middleware that let you define your URL routes. 

Routes let you map incoming requests to different applications based on the request verb and path. A route is composed
of a path pattern, an optional set of verbs to match, and an application endpoint: 

```java
server.start(new DynamicRoutes() {{
    get("/posts/:id").to((request, response) -> {
        // retrieve a given post
    });
    post("/posts").to((request, response) -> {
        // create a new post
    }); 
    put("/posts/:id").to((request, response) -> {
        // update an existing post
    });
    delete("/posts/:id").to((request, response) -> {
        // delete a post
    }); 
    map("/").to((request, response) -> {
        // show the home page
    });
}});
```
### Matching

Routes are matched in the order they are defined. If not defined route matches, the default behaviour is to 
render a 404 Not Found. This can be configured to pass the control to any default application.

By default, a route matches a single verb, specified by the method you use, i.e. _get_, _post_, _put_, _delete_.
That can be changed by providing the verbs as arguments to the _via_ method:

```java
map("/").via(GET, HEAD).to((request, response) -> {
    // show the home page
});
```

If you don't provide any verbs, _map_ will match on all verbs.

### Dynamic Parameters

Route patterns can be matched exactly - they are said to be static - or can include named parameters,
 which are then accessible as regular request parameters on the request object:

```java
// matches "GET /photos/18" and "GET /photos/25"
// request.parameter("id") is either '18' or '25'
get("/photos/:id", (request, response) -> {
    response.done("Photo #" + request.parameter("id"));
});
```

### Custom Matching

You are not limited to the provided match patterns. You can easily implement your own matcher and decide exactly how to match an incoming url to an application.

To do this, use the route definition methods that accept a _Matcher_ rather than a _String_.


## Working with the Request

Applications receive a <code>Request</code> that provide information about the request coming in from the client.

The request represents the environment for the client request, including the headers, the request body as well as parameters and other common things. The <code>Request</code> object is built from information provided by the HTTP server. 

Any middleware can modify the content of the request during processing before passing control to the next stage of the middleware pipeline. This of course has no effect on the original HTTP request. See [Middlewares](#middlewares) for more information on middlewares and the middleware pipeline.

### Request

```java
request.uri();                          // the uri, e.g. /path?query
request.path();                         // the path info, e.g. /foo
request.remoteIp();                     // ip of the client
request.remoteHost();                   // hostname of the client
request.remotePort();                   // port of the client
request.protocol();                     // protocol, e.g. HTTP or HTTPS
request.timestamp();                    // time the request came in
request.secure();                       // whether the request was made over a secure connection
request.method();                       // HTTP method (e.g.  GET, POST, PUT, etc.)
request.body();                         // the body as a string, decoded using the request charset
request.bodyContent();                  // the raw body content as bytes
request.bodyStream();                   // the body as a stream of bytes
request.parts();                        // list of parts of a multipart/form-data request
request.part("name");                   // named part of a multipart/form-data request
request.charset();                      // charset of the body, read from the content type
request.hasHeader("name");              // checks presence of a named header
request.header("name");                 // value of a given HTTP header
request.headers("name");                // list of values of a given HTTP header
request.headerNames();                  // the set of HTTP header names received
request.contentLength();                // length of the body
request.contentType();                  // content type of the body
request.parameter("name");              // value of a specific request parameter
request.paremeters("name");             // list of values of a specific request parameter
request.parameterNames();               // set of all request parameter names
request.allParameters();                // map of all request parameters
request.attribute("key");               // value of a keyed attribute
request.attribute("key", "value");      // sets the value of a keyed attribute
request.attributeKeys();                // set of all attibute keys
request.removeAttribute("key");         // removes a keyed attribute
request.attributes();                   // map of all request attributes
```

For the complete documentation, see the Javadoc of the <code>Request</code> class.

### Attributes

Request attributes are not sent by the client - as opposed to request parameters. They are used for server-side processing only.

Attributes are a local server storage mechanism, scoped within the request. Whereas request parameters are string literals, request attributes can be any type of <code>Object</code>s.

## Working with the Response

Applications respond to client requests by sending data back to the client using the <code>Response</code>. 

The response includes a status code and text, headers and an optional body. 

Any middleware can modify the content of the response during processing before returning control to the previous stage of the middleware pipeline. See [Middlewares](#middlewares) for more information on middlewares and the middleware pipeline.

### Response

```java
response.status(HttpStatus.OK);         // sets the status
response.statusCode(400);               // sets the status code
response.statusText("Bad Request");     // sets the status text
response.redirect("/url");              // 303 redirect to /url
response.header("name", "value");       // sets the single value of a named header
response.addHeader("name", "value");    // adds another value to a named header
response.contentType("text/html");      // sets the Content-Type header of the response
response.contentLength(16384);          // sets the Content-Length header of the response
response.charset("utf-8");              // sets the charset of the response body
response.body("response text");         // sets the response body as text
response.done();                        // sends the response to the client
```
For the complete documentation, see the Javadoc of the <code>Response</code> class.

Note that no response will actually be sent back to the client until the <code>done</code> is called.
Calling <code>done</code> signals the end of the request processing and triggers sending back the status, 
headers and body.

### Bodies

Response bodies can be sent back to the client either as text, binary content, or as <code>Body</code> objects.

A <code>Body</code> provides an abstraction for representing data to be sent back to the client.

Molecule comes with a few body implementations ready to use. For instance, you can use a <code>FileBody</code> to send back content of a file to the client.

### Redirection

You can trigger a browser redirect using a See Other (303) status code 
using the <code>redirectTo</code> method on the <code>Response</code>:

```java
response.redirectTo("/url");
```

If you need to use a different status code, simply change the status: 

```java
response.redirectTo("/url").statusCode(301); // moved permanently
```

## Cookies

To enable cookies support first add the <code>Cookies</code> middleware to your middleware pipeline (see [Middlewares](#middlewares) for more information on using middlewares).

This will create a <code>CookieJar</code> and populate that cookie jar with cookies sent by the client. The cookie jar is available as a request attribute:

```java
CookieJar cookies = CookieJar.get(request);
```

To read cookies sent by the client:
```java
Cookie customer = cookies.get("customer");
```

To send back cookies to the client, all you have to do is add cookies to the jar:
```java
cookies.add("weapon", "rocket launcher")
       .path("/ammo")
       .maxAge(30)
```

To expire a cookie, discard it from the jar:
```java
cookies.discard("weapon").path("/ammo");
```

For more on cookies, see the [Cookies example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/cookies/CookiesExample.java).

## Sessions

## Rendering Templates

## View Layouts

## SSL

## Testing

## Middlewares

Middlewares are a way to enhance your application with optional building blocks, using a pipeline design. A middleware component sits between the client and the server, processing inbound requests and outbound responses.

Middlewares implement functionality you tend to need across all your applications,
but you don't want to build everytime. Things like **access logging**, **authentication**, 
**compression**, **static files**, **routing**, etc.

Being able to separate the processing of the request (and post-processing of the response) in different stages 
has several benefits:

* It separate concerns, which helps keep your design clean and application well-structured
* It let you only include the functionality you need, so your server is as small and fast as possible 
* It let you plug in your own processing stages, to customize the behavior of your application
* It let you reuse and share middlewares, as elemental building blocks of application behavior

For example you could have the following separate stages of the pipeline doing:

1. Capturing internal server errors to render a nice 500 page
1. Monitoring, logging accesses to the server
1. Authentication and authorisation, to control access to your applicatoin
1. Caching, returning a cached result if request has already been processed recently
1. Compression, to reduce bandwith usage
1. Security, to prevent attacks such as CSRF
1. Processing, the actual request processing by your application
 
### The Middleware Stack

You can think of the Middleware pipeline as a stack, at the bottom of which is your application. When a request comes in, it starts at the top of the stack and goes down until it is processed by your application. The response then goes up the stack backwards through the middleware chain, before it is sent back to the client. 

We call it a middleware stack because each part will call the next part in sequence. Or it might not. In fact there are two types of middlewares. Those that modify the request or response and call the next step in the chain, and those that short circuit the stack and return their own response without ever calling anything further down the stack.


### Building the Middleware Stack

The simplest way to build your middleware stack is to add middlewares to your WebServer. 

For example, suppose we'd like to enhance performance of our application by adding caching and compression:

```java
WebServer server = WebServer.create();
server.add(new ContentLengthHeader())
      .add(new ConditionalGet())
      .add(new ETag())
      .add(new Compressor())
      .start(new DynamicRoutes() {{
          // ...
      }});
```

Here's what a more complete, real-life middleware stack might look like:
```java
server.failureReporter(failureReporter)
      .add(new ServerHeader("Simple/6.0.1"))
      .add(new DateHeader())
      .add(new ApacheCommonLogger(logger))
      // a custom middleware to redirect non secure requests to HTTPS 
      .add(new ForceSSL())
      .add(new ContentLengthHeader())
      .mount("/api", new MiddlewareStack() {{
          use(new Failsafe());
          use(new FailureMonitor(failureReporter));
          use(new ConnectionScope(database));
          // runs the api application
          run(api());
      }})
      .add(new ConditionalGet())
      .add(new ETag())
      .add(new Compressor().compressibleTypes(CSS, HTML))
      // configures the StaticAssets middleware
      .add(staticAssets())
      .add(new HttpMethodOverride())
      .add(new Cookies())
      // a custom middleware to redirect based on the preferred user locale
      .add(selectLocale())
      // a custom middleware to display static pages if case of errors
      .add(new PublicExceptions())
      .add(new Failsafe())
      .add(new FailureMonitor(failureReporter))
      .add(new ConnectionScope(dataSource))
      .add(new CookieSessionTracker(CookieSessionStore.secure("secret")))
      .add(new Flash())
      // starts the main application
      .start(webapp());
```

For more on using middlewares, take a look at the various code examples (see above), 
including the [sample application](https://github.com/testinfected/simple-petstore/blob/master/webapp/src/main/java/org/testinfected/petstore/PetStore.java).

The javadoc of the <code>WebServer</code> class is another good source of information. 


### Available Middlewares

Molecule comes with a bunch of handy middlewares that you can use to build your processing pipeline:

* Router (See [Routing](#routing))
* Static Assets 
* File Server
* Apache Common Logger
* Apache Combined Logger
* Cookies (See [Cookies](#cookies))
* Locale Negotiation
* Compressor
* ETag
* Conditional Get
* Connection Scope
* Server Header
* Date Header
* Content-Length Header
* Filter Map
* URL Map
* Cookie Session Tracker
* Failsafe
* Failure Monitor
* Not Found
* Http Method Override
* Layout
* Flash


