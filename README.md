[![Stories Ready](https://badge.waffle.io/testinfected/molecule.png?label=ready&title=Ready)](https://waffle.io/testinfected/molecule)
[![Stories In Progress](https://badge.waffle.io/testinfected/molecule.png?label=In%20Progress&title=Started)](https://waffle.io/testinfected/molecule)
[![Build Status](https://travis-ci.org/testinfected/molecule.png?branch=master)](https://travis-ci.org/testinfected/molecule)

## Getting started

Build yourself using Buildr (for latest and greatest version), or simply download using Maven:

```xml
<dependency>
      <groupId>com.vtence.molecule</groupId>
      <artifactId>molecule</artifactId>
      <version>0.2</version>
</dependency>
```

Then you're ready to go:
```java
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        Server server = new SimpleServer(8080);

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
* [Hello World](https://github.com/testinfected/molecule/blob/master/examples/hello-world/src/com/vtence/molecule/examples/helloworld/HelloWorld.java)
* [Routing](https://github.com/testinfected/molecule/blob/master/examples/routing/src/com/vtence/molecule/examples/routing/Routing.java)
* [Server Configuration Options](https://github.com/testinfected/molecule/blob/master/examples/configuration/src/com/vtence/molecule/examples/configuration/ServerConfiguration.java)
* [Using Sessions](https://github.com/testinfected/molecule/blob/master/examples/sessions/src/com/vtence/molecule/examples/session/EnablingSessions.java)
* [Available Middlewares - aka Filtering](https://github.com/testinfected/molecule/blob/master/examples/middlewares/src/com/vtence/molecule/examples/middlewares/Middlewares.java)
* [Simply RESTful](https://github.com/testinfected/molecule/blob/master/examples/REST/src/com/vtence/molecule/examples/rest/REST.java)
* [PetStore](https://github.com/testinfected/simple-petstore/blob/master/webapp/src/main/java/org/testinfected/petstore/PetStore.java)
