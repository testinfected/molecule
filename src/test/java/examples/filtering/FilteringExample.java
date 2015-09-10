package examples.filtering;

import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.AbstractMiddleware;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;

public class FilteringExample {

    private final Map<String, String> users = new HashMap<String, String>();

    public FilteringExample() {
        users.put("admin", "admin");
    }

    public void run(WebServer server) throws IOException {
        // An an authentication filter that checks against a map of authorized users
        Middleware authenticate = new AbstractMiddleware() {
            public void handle(Request request, Response response) throws Exception {
                String user = request.parameter("username");
                String password = request.parameter("password");

                String token = users.get(user);
                if (password != null && password.equals(token)) {
                    request.attribute("user", user);
                    // Carry on with the processing chain
                    forward(request, response);
                } else {
                    // Halt request processing
                    response.status(UNAUTHORIZED)
                            .body("Get away!")
                            .done();
                }
            }
        };

        // All requests to /private/... go through the authentication filter
        server.filter("/private", authenticate)
              .start(new DynamicRoutes() {{
                  // This route is private thus requires authentication
                  get("/private/area").to((request, response) ->
                          response.body("Hello, " + request.attribute("user") + "!")
                                  .done());

                  // This route is public
                  get("/hello").to((request, response) ->
                          response.body("Welcome, Guest!")
                                  .done());
              }});
    }

    public static void main(String[] args) throws IOException {
        FilteringExample example = new FilteringExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/private/area?username=admin&password=admin");
    }
}