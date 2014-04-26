[![Stories Ready](https://badge.waffle.io/testinfected/molecule.png?label=ready&title=Ready)](https://waffle.io/testinfected/molecule)
[![Stories In Progress](https://badge.waffle.io/testinfected/molecule.png?label=In%20Progress&title=Started)](https://waffle.io/testinfected/molecule)
[![Build Status](https://travis-ci.org/testinfected/molecule.png?branch=master)](https://travis-ci.org/testinfected/molecule)
[![Coverage Status](https://coveralls.io/repos/testinfected/molecule/badge.png)](https://coveralls.io/r/testinfected/molecule)

## Getting started

Build yourself using [Gradle](http://www.gradle.org) or [Buildr](http://buildr.apache.org), or simply download from Maven Central:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.4-SNAPSHOT</version>
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

Then you're ready to go:
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

Your application is now available at:
`http://localhost:8080`

## Want to know more?

Check out the examples:
* [Hello World](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/helloworld/HelloWorldExample.java)
* [Basic Functionality](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/basic/BasicExample.java)
* [Routing](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/routing/RoutingExample.java)
* [Sessions](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/session/SessionExample.java)
* [REST](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/rest/RESTExample.java)
* [Static Files](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/files/StaticFilesExample.java)
* [View Templating](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/templating/TemplatingExample.java)
* [Middlewares](https://github.com/testinfected/molecule/blob/master/src/test/java/examples/middlewares/MiddlewaresExample.java)
* [PetStore](https://github.com/testinfected/simple-petstore/blob/master/webapp/src/main/java/org/testinfected/petstore/PetStore.java)
