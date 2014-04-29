package examples.filtering;

import com.vtence.molecule.Application;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.AbstractMiddleware;
import com.vtence.molecule.lib.Middleware;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FilteringExample {

    private final Map<String, String> users = new HashMap<String, String>();

    public FilteringExample() {
        users.put("admin", "admin");
    }

    public void run(WebServer server) throws IOException {
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
                    response.status(HttpStatus.UNAUTHORIZED).body("Get away!");
                }
            }
        };

        server.filter("/private", authenticate)
              .start(new DynamicRoutes() {{
                  get("/private/area").to(new Application() {
                      public void handle(Request request, Response response) throws Exception {
                          response.body("Hello, " + request.attribute("user"));
                      }
                  });

                  get("/hello").to(new Application() {
                      public void handle(Request request, Response response) throws Exception {
                          response.body("Welcome");
                      }
                  });
              }});
    }

    public static void main(String[] args) throws IOException {
        WebServer webServer = WebServer.create();
        FilteringExample example = new FilteringExample();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/private/area?username=admin&password=admin");
    }
}
