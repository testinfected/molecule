package com.vtence.molecule.examples.helloworld;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;

/**
 * Access at <a href="http://localhost:8080">http://localhost:8080</a>
 */
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
