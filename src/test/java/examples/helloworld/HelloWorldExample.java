package examples.helloworld;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

/**
 * In this example, we run a single application that responds <i>Hello, World!</i> to all incoming requests.
 * This is as simple as it can get.
 */
public class HelloWorldExample {

    public void run(WebServer server) throws IOException {
        // Start the default web server and provide a single application, which
        // responds to all incoming requests.
        server.start(Application.of(request -> Response.ok().done("Hello, World!")));
    }

    public static void main(String[] args) throws IOException {
        HelloWorldExample example = new HelloWorldExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
