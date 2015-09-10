package examples.routing;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;

public class RoutingExample {

    public void run(WebServer server) throws IOException {
        server.start(new DynamicRoutes() {{

            map("/").to((request, response) -> response.body("Welcome!").done());

            post("/login").to((request, response) -> {
                String username = request.parameter("username");
                response.redirectTo("/users/" + username)
                        .done();
            });

            get("/hello/:username").to((request, response) -> {
                response.contentType("text/html");
                response.body(
                        "<html>" +
                        "<body>" +
                            "<h3>Hello, " + request.parameter("username") + "</h3>" +
                        "</body>" +
                        "</html>"
                );
                response.done();
            });
        }});
    }

    public static void main(String[] args) throws IOException {
        RoutingExample example = new RoutingExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}