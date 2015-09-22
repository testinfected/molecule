package examples.helloworld;

import com.vtence.molecule.WebServer;

import java.io.IOException;

public class HelloWorldExample {

    public void run(WebServer server) throws IOException {
        server.start((request, response) -> response.done("Hello, World!"));
    }

    public static void main(String[] args) throws IOException {
        HelloWorldExample example = new HelloWorldExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}