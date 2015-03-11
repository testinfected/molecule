package examples.cookies;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

public class CookiesExample {

    public void run(WebServer server) throws IOException {
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
                String profile = "wine lover";
                String location = "quebec";
                response.body(String.format("profile: %s, location: %s", profile, location));
            }
        });
    }

    public static void main(String[] args) throws IOException {
        CookiesExample example = new CookiesExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}