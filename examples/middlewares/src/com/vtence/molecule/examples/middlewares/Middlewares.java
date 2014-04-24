package com.vtence.molecule.examples.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.middlewares.AbstractMiddleware;
import com.vtence.molecule.middlewares.ApacheCommonLogger;
import com.vtence.molecule.middlewares.Compressor;
import com.vtence.molecule.middlewares.ContentLengthHeader;
import com.vtence.molecule.middlewares.DateHeader;
import com.vtence.molecule.middlewares.Failsafe;
import com.vtence.molecule.middlewares.FailureMonitor;
import com.vtence.molecule.middlewares.FilterMap;
import com.vtence.molecule.middlewares.HttpMethodOverride;
import com.vtence.molecule.middlewares.MiddlewareStack;
import com.vtence.molecule.middlewares.ServerHeader;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.util.PlainErrorReporter;

import java.io.IOException;
import java.util.logging.Logger;

import static com.vtence.molecule.middlewares.Router.draw;
import static java.lang.Integer.parseInt;

public class Middlewares {

    public static void main(String[] args) throws IOException {
        SimpleServer server = new SimpleServer(8080);

        server.run(new MiddlewareStack() {{
            // Add information about our server
            use(new ServerHeader("MyApp/1.0 molecule/0.3"));
            // Also include date and time of the response
            use(new DateHeader());
            // Log all accesses to the default JDK logger
            use(new ApacheCommonLogger(Logger.getAnonymousLogger()));
            // Set content length header on responses with fixed-length bodies
            use(new ContentLengthHeader());
            // Eat up all internal server errors and respond with a 500 page
            use(new Failsafe());
            // Print internal server errors to the standard error stream
            use(new FailureMonitor(PlainErrorReporter.toStandardError()));
            // Support HTTP method override via the _method request parameter
            use(new HttpMethodOverride());
            // Compress response content using gzip or deflate when acceptable by the client
            use(new Compressor());

            // A filter map applies middleware depending on the request
            FilterMap filters = new FilterMap();
            // All requests to /private halt with 403
            filters.map("/private", new AbstractMiddleware() {
                public void handle(Request request, Response response) throws Exception {
                    response.status(HttpStatus.FORBIDDEN);
                    response.body("This is a restricted area");
                    // Stop the processing chain
                }
            });
            // All requests with / prefix (i.e. all other requests) go through this dummy
            // authentication filter
            filters.map("/", new AbstractMiddleware() {
                public void handle(Request request, Response response) throws Exception {
                    // At this point we would perform proper authentication
                    String username = "...";
                    request.attribute("username", username);
                    // Carry on with the processing chain
                    forward(request, response);
                }
            });
            use(filters);

            // You can add your own middleware to wrap request processing
            use(new AbstractMiddleware() {
                public void handle(Request request, Response response) throws Exception {
                    // The 'before' part
                    response.contentType("text/plain");
                    // Carry on with the processing chain
                    forward(request, response);
                    // The 'after' part
                    response.status(HttpStatus.OK);
                }
            });

            run(draw(new DynamicRoutes() {{
                get("/orders/:id").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        response.body("Order #" + parseInt(request.parameter("id")));
                    }
                });

                post("/orders").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        response.body("Submitted order: " + request.body());
                    }
                });

                // Access with either a PUT or a POST with _method=PUT
                put("/orders/:id").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        response.body(String.format("Revised order #%d: %s",
                                parseInt(request.parameter("id")), request.body()));
                    }
                });

                // Access with either a DELETE or a POST with _method=DELETE
                delete("/orders/:id").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        response.body("Cancelled order #" + parseInt(request.parameter("id")));
                    }
                });
            }}));
        }});
    }
}
