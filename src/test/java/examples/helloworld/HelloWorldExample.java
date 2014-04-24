package examples.helloworld;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

public class HelloWorldExample {

    public void run(WebServer server) throws IOException {
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });
    }

    public static void main(String[] args) throws IOException {
        // Run the default web server
        WebServer webServer = WebServer.create();
        HelloWorldExample example = new HelloWorldExample();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}