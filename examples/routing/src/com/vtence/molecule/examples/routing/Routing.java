package com.vtence.molecule.examples.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.middlewares.Router;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;

public class Routing {

    public static void main(String[] args) throws IOException {
        Server server = new SimpleServer(8080);

        server.run(Router.draw(new DynamicRoutes() {{

            map("/").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.body("Welcome!");
                }
            });

            get("/hello").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.body("Hello, World");
                }
            });

            post("/login").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    String username = request.parameter("username");
                    response.redirectTo("/users/" + username);
                }
            });

            get("/users/:username").to(new Application() {
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

            map("/private").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.status(HttpStatus.UNAUTHORIZED);
                    response.body("Go away!");
                }
            });
        }}));
    }
}
