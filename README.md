[![Build Status](https://travis-ci.org/testinfected/molecule.png?branch=master)](https://travis-ci.org/testinfected/molecule)
[![Coverage Status](https://coveralls.io/repos/testinfected/molecule/badge.png)](https://coveralls.io/r/testinfected/molecule)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.vtence.molecule/molecule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.vtence.molecule/molecule)
[![Issues In Progress](https://badge.waffle.io/testinfected/molecule.png?label=In%20Progress&title=Started)](https://waffle.io/testinfected/molecule)

## Getting started

```java
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        WebServer server = WebServer.create();
        server.run((request, response) -> response.body("Hello, World"));
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
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });
    }
}
```

## Download 

Get the latest release version from Maven Central:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.4</version>
</dependency>
```
 
If you want the development version, grab the latest snapshot from Sonatype snapshots repositories 
(```https://oss.sonatype.org/content/repositories/snapshots```):

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.5-SNAPSHOT</version>
</dependency>
```

To use the default web server, you need to add [Simple](http://www.simpleframework.org) as a dependency:

```xml
<dependency>
      <groupId>org.simpleframework</groupId>
      <artifactId>simple</artifactId>
      <version>5.1.6</version>
</dependency>
```

## Want to know more?

Check out the examples:
* [Hello World](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/helloworld/HelloWorldExample.java)
* [Rendering HTML](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/simple/SimpleExample.java)
* [Routing](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/routing/RoutingExample.java)
* [Static Files](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/files/StaticFilesExample.java)
* [REST](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/rest/RESTExample.java)
* [View Templates and Layout](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingAndLayoutExample.java)
* [HTTP Sessions](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/session/SessionExample.java)
* [Filters](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/filtering/FilteringExample.java)
* [Custom Middleware](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/middleware/CustomMiddlewareExample.java)
* [Caching and Compression](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/performance/CachingAndCompressionExample.java)
* [Sample Application](https://github.com/testinfected/simple-petstore/blob/master/webapp/src/main/java/org/testinfected/petstore/PetStore.java)
