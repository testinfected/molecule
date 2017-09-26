package examples.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;

/**
 * <p>
 * This is a simple example showing how to define routes and how to use route patterns containing named
 * parameters. Named parameters are accessible as normal request parameters.
 * </p>
 * <p>
 * We want to route incoming requests based on their paths and verbs, so we provide route patterns as strings.
 * If your matching logic is more sophisticated, you can also provide custom matchers.
 * </p>
 */
public class RoutingExample {

    public void run(WebServer server) throws IOException {
        // We start the server by providing a set of routes, which creates a router to dispatch
        // requests to different endpoints.
        server.start(new DynamicRoutes() {{
            // Routes are matched in the order they are defined. The first one that matches the request will be
            // invoked.

            // Route all requests to the root path - ignoring the verb - to an endpoint that responds with "Welcome!"
            map("/").to(Application.of(request -> Response.ok().done("Welcome!")));

            // Route POST requests to /login to an endpoint that simulates login. It simply reads the username
            // from the request parameters and redirects to /users/username.
            post("/login").to(Application.of(request -> {
                String username = request.parameter("username");
                return Response.redirect("/hello/" + username)
                               .done();
            }));

            // Route GET requests to /hello/username to an endpoint that renders and HTML page with
            // a greeting message for our user. We use a route pattern that includes a named segment (:username).
            // The bound parameter is available as a request parameter.
            get("/hello/:username").to(
                    Application.of(request -> Response.ok()
                                                      .contentType("text/html")
                                                      .done("<html>" +
                                                            "<body>" +
                                                            "<h3>Hello, " + request.parameter("username") + "</h3>" +
                                                            "</body>" +
                                                            "</html>"
                                                      )));
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
