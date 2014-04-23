package examples.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;
import java.net.InetAddress;

import static com.vtence.molecule.middlewares.Router.draw;

public class RoutingExample {

    public void run(Server server) throws IOException {
        server.run(draw(new DynamicRoutes() {{
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

    public static void main(String[] args) throws IOException {
        // By default, server will run on a random available port...
        SimpleServer server = new SimpleServer();
        new RoutingExample().run(server);
        System.out.println("Running on http://" + InetAddress.getLocalHost().getHostName() + ":" + server.port());
    }
}
