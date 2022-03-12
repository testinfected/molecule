package examples.http2;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

/**
 * In this example, we run Undertow (the default server) with HTTP/2 enabled.
 */
public class Http2Example {

    public void run(WebServer server) throws IOException {
        server.enableHTTP2()
              .start(request -> Response.ok().done("Running with HTTP/2"));
    }

    public static void main(String[] args) throws IOException {
        Http2Example example = new Http2Example();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
