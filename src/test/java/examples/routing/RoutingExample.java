package examples.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;

public class RoutingExample {

    public void run(WebServer server) throws IOException {
        server.start(new DynamicRoutes() {{

            map("/").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.body("Welcome!");
                }
            });

            post("/login").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    String username = request.parameter("username");
                    response.redirectTo("/users/" + username);
                }
            });

            get("/hello/:username").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.contentType("text/html");
                    response.body(
                            "<html>" +
                            "<body>" +
                                "<h3>Hello, " + request.parameter("username") + "</h3>" +
                            "</body>" +
                            "</html>"
                    );
                }
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
