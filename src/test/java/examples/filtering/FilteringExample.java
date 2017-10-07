package examples.filtering;

import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.routing.Routes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;

/**
 * <p>
 *     This example shows how to filter access to specific resources. We use a custom authentication middleware
 *     to make sure only authenticated users can access resources under <code>/private</code>.
 * </p>
 */
public class FilteringExample {

    /**
     * Our user database is a simple hash.
     */
    private final Map<String, String> users = new HashMap<>();

    // Let's populate our users base with a single user "admin" with password "admin"
    {
        users.put("admin", "admin");
    }

    public FilteringExample() {
    }

    public void run(WebServer server) throws IOException {
        // We implement a simple authentication middleware that checks against a map of authorized users
        // If credentials match, we allow access to the requested resource. If not, we send a 401 - Unauthorized.
        Middleware authenticate = next -> request -> {
                        // We read the username and password from the request parameters
                        String user = request.parameter("username");
                        String password = request.parameter("password");

                        String token = users.get(user);
                        if (password != null && password.equals(token)) {
                            // Credentials match, store the current user as a request attribute...
                            request.attribute("user", user);
                            // ... then carry on with the processing chain
                            return next.handle(request);
                        } else {
                            // Halt request processing
                            return Response.of(UNAUTHORIZED)
                                           .done("Get away!");
                        }
                    };

        // All requests to /private/... go through the authentication filter
        server.filter("/private", authenticate)
              .route(new Routes() {{
                  // This route is private, thus it requires authentication
                  get("/private/area").to(request -> Response.ok().done("Hello, " + request.attribute("user") + "!"));

                  // This route is public, anybody can access it
                  get("/hello").to(request -> Response.ok().done("Welcome, Guest!"));
              }});
    }

    public static void main(String[] args) throws IOException {
        FilteringExample example = new FilteringExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/private/area?username=admin&password=admin");
    }
}
