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
        server.start((request, response) -> response.body("Hello, World"));
    }
}
```

Access your application at:

`http://localhost:8080`

If you don't use Java 8, it's almost as good:

```java
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });
    }
}
```

## Download 

You can get the latest release version from Maven Central:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.8.2</version>
</dependency>
```
 
If you want the development version, grab the latest snapshot from Sonatype snapshots repositories 
(```https://oss.sonatype.org/content/repositories/snapshots```):

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.9-SNAPSHOT</version>
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

Try out the following examples (Java 6 language level):

* [Hello World](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/helloworld/HelloWorldExample.java)
* [Rendering HTML](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/simple/SimpleExample.java)
* [Dynamic Routes](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/routing/RoutingExample.java)
* [Static Files](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/files/StaticFilesExample.java)
* [REST](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/rest/RESTExample.java)
* [Cookies](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/cookies/CookiesExample.java)
* [Locale Negotiation](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/locale/LocaleNegotiationExample.java)
* [Multipart Forms](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/multipart/MultipartExample.java)
* [View Templates and Layout](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingAndLayoutExample.java)
* [HTTP Sessions](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/session/SessionExample.java)
* [Filters](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/filtering/FilteringExample.java)
* [Creating a Custom Middleware](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/middleware/CustomMiddlewareExample.java)
* [Caching and Compression](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/performance/CachingAndCompressionExample.java)
* [SSL](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/ssl/SSLExample.java)
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
server.start((request, response) -> response.body("Hello, World!"));
```

To stop the server, call the _stop_ method:

```java
server.stop()
```

You can optionally specify the interface and port to bound to when creating the server, e.g. if you want to make your server globally available:

```java
WebServer server = WebServer.create("0.0.0.0", 8088);
```

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
    response.body("Photo #" + request.parameter("id"));
});
```

### Custom Matching

You are not limited to the provided match patterns. You can easily implement your own matcher and decide exactly how to match an incoming url to an application.

To do this, use the route definition methods that accept a _Matcher_ rather than a _String_.


## Working with the Request

### Request Object

### Attributes

## Working with the Response

### Response Object

### Bodies

### Redirection and Errors

## Cookies

## Sessions

## Rendering Templates

## View Layouts

## Testing

## Middlewares

Middlewares are a way to enhance your application with optional building blocks, using a pipeline design. 

They implement functionality you tend to need across all your applications,
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
1. Authentication and authorisation, to control access to your applicatin
1. Caching, returning a cached result if request has already been processed recently
1. Compression, to reduce bandwith usage
1. Security, to prevent attacks such as CSRF
1. Processing, to actually process the request

### Available Middlewares

Molecule comes with a number of middlewares (more are coming), that you can use to build your processing pipeline:

* Router (See [Routing](http://https://github.com/testinfected/molecule#routing))
* Static Assets 
* File Server
* Access Log
* Cookies
* Locale Negotiation
* Compression
* ETag
* Conditional Get
* Connection Scope
* Server Header
* Date Header
* Content-Length Header
* Filter Map
* Cookie Session Tracker
* Fail Safe
* Failure Monitor
* Not Found
* Http Method Override
* Layout

