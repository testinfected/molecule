[![Build Status](https://travis-ci.org/testinfected/molecule.svg?branch=master)](https://travis-ci.org/testinfected/molecule)
[![Coverage Status](https://img.shields.io/coveralls/testinfected/molecule.svg?style=flat)](https://coveralls.io/r/testinfected/molecule)
[![Maven Central](https://img.shields.io/maven-central/v/com.vtence.molecule/molecule.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.vtence.molecule/molecule)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://vtence.mit-license.org)

## Quick Start

```java
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        server.start(request -> Response.ok().done("Hello, World"));
    }
}
```

Access your application at:

`http://localhost:8080`

## About

Molecule is an HTTP toolkit for Java, with no dependencies. It is
- fast and small,
- easy to use and extend,
- fully tested.

Molecule is built around the simple concept of Application as a Function:
- An `Application` is an asynchronous function of `(Request) -> Response`, typically representing some 
  remote endpoint or service.
- A `Middleware` is a simple function of `(Application) -> Application` that implements application-independent 
functionality. A middleware is composed with an application to modify application behavior.

Molecule is great for building micro services or regular web applications. It is designed around simplicity,
testability and freedom of choice. Built entirely using TDD it provides super-easy ways to test your 
application and individual endpoints, both in and out of container.

Molecule is small - it weights less than 150k - and will stay as lean as possible. It is pluggable through the concept
of middlewares (a.k.a filters). It offers various abstractions for a number of functionalities 
(such as routing, templating, security, etc.). You're free to use the built-in options or provide 
your own implementations. 

Molecule requires Java 8. It runs an embedded web-server powered by [Simple](http://simpleframework.org) 
or [Undertow](http://undertow.io). 
Both are fully asynchronous and non-blocking, which means they can scale to very high loads.

Have fun!

## Download 

You can get the latest release version from Maven Central:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.14.0</version>
</dependency>
```
 
To use Simple as your web server, add [Simple](http://www.simpleframework.org) as a dependency:

```xml
<dependency>
      <groupId>org.simpleframework</groupId>
      <artifactId>simple-http</artifactId>
      <version>6.0.1</version>
      <scope>runtime</scope>
</dependency>

```
To choose Undertow as your web server, add [Undertow](http://undertow.io) instead to your dependencies:

```xml
<dependency>
      <groupId>io.undertow</groupId>
      <artifactId>undertow-core</artifactId>
      <version>1.4.4.Final</version>
      <scope>runtime</scope>
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

This will set the web server to run locally on port 8080.

To start the server, give it an app:

```java
server.start(request -> Response.ok().done("Hello, World!"));
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

Molecule uses [Simple](http://www.simpleframework.org) as a default web server. You have the choice to run using [Undertow](http://undertow.io) instead. Both are fully asynchronous and non-blocking. This allows the server to scale to very high loads and handle as many concurrent connections as possible, even when depending on a high latency external resource.
         
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
server.route(new Routes() {{
    get("/posts/:id").to(request -> {
        // retrieve a given post
    });
    
    post("/posts").to(request -> {
        // create a new post
    }); 
    
    put("/posts/:id").to(request -> {
        // update an existing post
    });
    
    delete("/posts/:id").to(request -> {
        // delete a post
    }); 
    
    map("/").to(request -> {
        // show the home page
    })
}});
```

To start the server with the router use `Server#route`. For an example have a look at [Dynamic Routes](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/routing/RoutingExample.java).

### Matching

Routes are matched in the order they are defined. If not defined route matches, the default behaviour is to 
render a 404 Not Found. This can be configured to pass the control to any default application.

By default, a route matches a single verb, specified by the method you use, i.e. _get_, _post_, _put_, _delete_.
That can be changed by providing the verbs as arguments to the _via_ method:

```java
map("/").via(GET, HEAD).to(request -> {
    // show the home page
});
```

If you don't provide any verbs, _map_ will match on all verbs.

You can also match based on the HTTP `Accept` header, such as:

```java
get("/api/posts").accept("application/json").to(request -> {...});
```

### Dynamic Parameters

Route patterns can be matched exactly - they are said to be static - or can include named parameters,
 which are then accessible as regular request parameters on the request object:

```java
// matches "GET /photos/18" and "GET /photos/25"
// request.parameter("id") is either '18' or '25'
get("/photos/:id").to(request -> {
    Response.ok().done("Photo #" + request.parameter("id"));
});
```

### Custom Matching

You are not limited to the provided match patterns. You can provide your own predicate and decide exactly how to match 
an incoming url to an application.

To do this, use the route definition methods that accept a _Predicate_ rather than a _String_.


## Working with the Request

Applications receive a <code>Request</code> that provide information about the request coming in from the client.

The request represents the environment for the client request, including the headers, the request body as well as parameters and other common things. The <code>Request</code> object is built from information provided by the HTTP server. 

Any middleware can modify the content of the request during processing before passing control to the next stage of the middleware pipeline. This of course has no effect on the original HTTP request. See [Middlewares](#middlewares) for more information on middlewares and the middleware pipeline.

### Request

```java
request.uri();                          // the full uri, e.g. http://localhost:8080/foo?bar
request.url();                          // the full url, e.g. http://www.example.com/foo?bar
request.path();                         // the path info, e.g. /foo
request.query();                        // the query string, e.g. bar
request.remoteIp();                     // ip of the client
request.remoteHost();                   // hostname of the client
request.remotePort();                   // port of the client
request.protocol();                     // protocol, e.g. HTTP/1.1
request.scheme();                       // the scheme
request.hostname();                     // the hostname part of the HOST header
request.port();                         // the port part of the HOST header
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
request.hasParameter("name")            // checks for the presence of a request parameter
request.parameter("name");              // value of a specific request parameter
request.paremeters("name");             // list of values for a specific request parameter
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
response.header("name", "value");       // sets the single value of a named header
response.addHeader("name", "value");    // adds another value to a named header
response.contentType("text/html");      // sets the Content-Type header of the response
response.contentLength(16384);          // sets the Content-Length header of the response
response.charset("utf-8");              // sets the charset of the response body
response.body("text");                  // sets the response body as text
response.done();                        // sends the response to the client
response.done("text");                  // sends the specified response text to the client
```
For the complete documentation, see the Javadoc of the <code>Response</code> class.

Note that no response will actually be sent back to the client until the <code>done</code> method is called.
Calling <code>done</code> signals the end of the request processing and triggers sending back the status, 
headers and body.

### Rendering

There are a variety of ways to send back a response to the client. You can render text, binary content, 
the content of a file, XML, JSON, use a view template or render nothing at all. You can specify the content type or HTTP status of the rendered response as well.

Molecule uses the concept of response <code>Body</code> to represent data to send back to the client.

Molecule comes with a few body implementations ready to use for sending text, binary content, the content of a file or
 a view template.
 

#### Rendering an empty response

You can render an empty response by not specifying a body at all. In which case, an empty body is used. 

So this is perfectly valid:

```java
response.done();
```

#### Rendering text

You can send plain text - with no markup at all - back to the browser like this:

```java
response.done("All good");
```

Rendering pure text is sometimes useful if you're client is expecting something other than proper HTML.
                         
#### Rendering HTML
                         
You can send an HTML string back to the browser by using a text body to render:
                         
```java
response.done("<html><body><h1>It Works</h1></body></html>");
```

This can be useful when you're rendering a small snippet of HTML code. However, you might want to consider moving it to a template file if the markup is complex. See [View Templating](#view-templates) for more on using templates. 
                       
                       
#### Rendering JSON

You can send back JSON to the browser by using a text body. Here's an example using google `Gson` library:
 
```java
Gson gson = new Gson();
response.done(gson.toJSON("ok"));
``` 

Another option would be to create a your own body implementation - let's call it <code>JSONBody</code> - 
and use your preferred JSON serializer:
 
```java
response.done(new JSONBody("ok"));
```

or with a static factory method:

```java
response.done(json("ok"));
```

Rendering XML can be done the same way.


#### Rendering binary content

You can send back a raw body as binary like this:

```java
byte[] content = ... //
response.body(content).done();
```

#### Rendering the content of a file

You can use a <code>FileBody</code> to stream the content of a file:
 
 ```java
 response.render(new FileBody(new File("/path/to/file")))).done();
 ```
 
 This will use a default chunk size of <code>8K</code>, although you can specify a different chunk size.

#### Redirection

You can trigger a browser redirect using a See Other (303) status code :

```java
Response.redirect("/url").done();
```

If you need to use a different status code, simply change the status: 

```java
Response.redirect("/url", 301).done(); // moved permanently
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

### Enabling sessions support

To use sessions you need to start your server with session support:

```java
server.add(new Cookies())
      .add(new CookieSessionTracker(CookieSessionStore.secure("your secret");))
      .route(new Routes() {{
      // your routing here
      }});
```

Since HTTP is a stateless protocol and Molecule uses a minimalist approach, sessions are not part of the request. 
Usage of sessions requires the use of a middleware. This means you have full control over the way sessions are tracked and stored.

Molecule comes with a <code>CookieSessionTracker</code> middleware that uses cookies to track sessions across HTTP requests and two session storage mechanisms:
* An in memory session store, the <code>SessionPool</code>
* A client side storage using encrypted cookies, the <code>CookieSessionStore</code> 

### Using sessions

Once session support is enabled, the current session is bound to the request:

```java
Session session = Session.get(request);
```

One thing to understand is that as long as your server is started with session support, there will *always* be a session bound to the request. There's no need to check against <code>null</code>. There's no need to ask for session creation either. 

The session attached to the request can be a fresh and empty session or the session opened in a previous request. This does not mean a new session is automatically opened for each request though. A fresh session is only persisted if it is modified before the end of the request cycle. This means you can safely read from a new session. 

If you write data to the session then it is automatically persisted to the session store you've selected - created in case of a new session or updated in case of an existing session. If you invalidate the session, it will be discarded from the session store automatically.

Some of the things you can do with sessions include:
```
session.fresh()             // checks if session is new
session.id()                // gets the session id
session.createdAt()         // the session creation time
session.updatedAd()         // the last session update time
session.expires()           // whether this session expires
session.expirationTime()    // this session expiration time, if it expires
session.maxAge(30)          // sets the session to expire after 30s of inactivity
session.contains("key")     // checks if this session contains a keyed attribute
session.get("key")          // returns the keyed attribute
session.remove("key")       // removes a keyed attribute
session.put("key", "value") // writes a key value pair to the session
session.clear()             // clears the session content
session.invalidate()        // invalidates the session
```

For more on using sessions, see the [Session Example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/session/SessionExample.java).

## View Templates

When your markup becomes complex, you might want to consider moving it to a template file.

Rendering a template requires a <code>RenderingEngine</code>. To use the built-in <code>JMustacheRenderer</code>, first
  add [JMustache](https://github.com/samskivert/jmustache) to your dependencies:
  
```xml
<dependency>
    <groupId>com.samskivert</groupId>
    <artifactId>jmustache</artifactId>
    <version>1.13</version>
</dependency>
```

Rendering a template takes a view model and returns a body object to use with the response. Assuming
 a Mustache template file named <i>profile.mustache</i>, here's how we would render the template 
 using an hypothetical <code>Employee</code> object: 
  
```java
// Declare the template engine
Templates templates = new Templates(JMustacheRenderer.fromDir(new File("/path/to/template/files")));
// Load the 'profile.mustache' template
Template<Employee> profile = templates.named("profile");
// Render the template using an Employee instance 
response.done(profile.render(new Employee("Bob", "...")));
```  
  
For further information on using view templates, take a look at the [View Templates and Layout example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingAndLayoutExample.java).
 
## View Layouts

Sometimes you want to share a common look and feel for a set of pages. This is when view layouts can help.
 
Layouts are just templates that are used to decorate existing pages. A layout defines the surroundings of your HTML page. 
It's where you define a common look and feel of your final output. This reverses the common pattern of 
including shared headers and footers in many templates to isolate changes in repeated setups.

Think of the layout rendering process in two steps. 

The first step processes the generated HTML view to extract its content. 
This extraction process makes individual pieces of the original page content available to the layout template:

* the head content, excluding the title
* the title
* the body content
* all the meta parameters

The second step merges the content pieces with the layout template to produce the final
result. 

Here's a contrived example of a mustache layout template that simply recreates the original page:

```html
<html>
<head>
{{head}}
<title>{{title}}</title>
</head>
<body>
{{body}}
</body>
</html>
```
  
The <code>Layout</code> middleware intercepts generated HTML responses and merges them with layout decorator(s) 
to build the final result:

```java
// Declare the template engine
Templates layouts = new Templates(JMustacheRenderer.fromClasspath("/path/to/layout/templates"));
// Load the main layout template
Template<Map<String, String>> mainLayout = layouts.named("main");

// Apply the main site layout to requests under the / path, in other words to all rendered pages
server.filter("/", Layout.html(mainLayout))
      .route(new Routes() {{
          // Your routes definitions here
          // ...
      }});

``` 

For more on using layouts, take a look at the [View Templates and Layout example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingAndLayoutExample.java).

## SSL

To use HTTPS connections, enable SSL on the <code>WebServer</code>. Enabling HTTPS/SSL requires you to have a keystore file, which you can generate using the Java `keytool`. You specify the location of your keystore, the keystore password 
and the keys password like this:

```java
WebServer server = WebServer.create();
server.enableSSL(new File("/path/to/keystore/file"), "keyStorePassword", "keyPassword"))
      .start(...);
```

This will start the server with TLS enabled, trusting all clients.

If you need more control over the type of keystore and algorithms used, you can alternatively pass 
an <code>javax.net.ssl.SSLContext</code> to use.

See the [SSL example](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/ssl/SSLExample.java) 
for more details.

## Testing

Molecule provides fluent assertion helpers for unit testing your application endpoints and middlewares.
 It also provides a test HTTP client for integration testing your server.
 
The test helpers are located in the `com.vtence.molecule.testing` package. To use them, you need to include [Hamcrest](http://hamcrest.org/JavaHamcrest/) to your list of dependencies:

```xml
<dependency>
    <groupId>org.hamcrest</groupId>
    <artifactId>hamcrest</artifactId>
    <version>2.2</version>
    <scope>test</scope>
</dependency>
```

If you want to use charset detection, you will also need [JUniversalChardet](https://github.com/thkoch2001/juniversalchardet): 

```xml
<dependency>
    <groupId>com.googlecode.juniversalchardet</groupId>
    <artifactId>juniversalchardet</artifactId>
    <version>1.0.3</version>
    <scope>test</scope>
</dependency>
```

### Unit Testing Middlewares and Application Endpoints
 
The `Request` and `Response` are plain Java objects that you can use directly in your unit tests. They are simple 
representations of the environment for the client request  and the content of the response your application 
wants to send back. They are not wrappers around the actual HTTP request and response, so there's no need to mock
 them:
 
```java
// Call your application endpoint
Response response = application.handle(Request.get("/")
                                              .header("Authorization", "Basic " + mime.encode("joe:bad secret")));
 
// Make assertions on the response
// ...

```

#### Fluent Assertions

`ResponseAssert` provides a fluent interface for asserting the generated response. Based on
the previous example:

```java
// ...
assertThat(response).hasStatus(UNAUTHORIZED)
                    .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                    .hasContentType("text/plain")
                    .isEmpty()
                    .isDone();
```

`RequestAssert` helps make assertions on the request:

```java
assertThat(request).hasAttribute("username", "joe");
```

`CookieJarAssert` and `CookieAssert` both provide fluent assertions for `Cookie`s and the `CookieJar`: 

```java
assertThat(cookieJar).hasCookie("session")
                     .hasValue("sJaAMRYetEJhQcS9s283pMrlGoXr88LkFf0NCOTZb8A")
                     .isHttpOnly();
```

### Integration Testing

You can use the test HTTP client to write integration tests for your API or 
test how various parts of your web application interact:
 
```java
HttpRequest request = new HttpRequest(8080).followRedirects(false);
HttpResponse response = request.content(Form.urlEncoded().addField("username", "bob")
                                                         .addField("password", "secret"))
                               .post("/login");
```

#### Fluent Assertions

`HttpResponseAssert` and `HttpCookieAssert` provides fluent interfaces for making assertions on the HTTP response
and cookies:

```java
assertThat(response).hasStatusCode(303)
                    .hasHeader("Location", "/users/bob")
                    .hasCookie("session").hasMaxAge(300);
```

## Middlewares

Middlewares are a way to enhance your application with optional building blocks, using a pipeline design. 
A middleware component sits between the client and the server, processing inbound requests and outbound responses.

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
      .route(new Routes() {{
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


## Living on the edge


If you want the latest development version, grab the latest snapshot from [Sonatype snapshots repositories]:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.15.0-SNAPSHOT</version>
</dependency>
```

New snapshots are pushed to Sonatype on every commit. So you'll always be running the head version. 

[Sonatype snapshots repositories]: https://oss.sonatype.org/content/repositories/snapshots
