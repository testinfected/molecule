package examples.routing;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.http.DeprecatedHttpRequest;
import com.vtence.molecule.support.http.DeprecatedHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RoutingTest {

    RoutingExample routing = new RoutingExample();
    WebServer server = WebServer.create(9999);

    DeprecatedHttpRequest request = new DeprecatedHttpRequest(9999);
    DeprecatedHttpResponse response;

    @Before
    public void startServer() throws IOException {
        routing.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void mappingRoutesToUrls() throws IOException {
        response = request.get("/");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo("Welcome!");
    }

    @Test
    public void restrictingARouteToASpecificVerb() throws IOException {
        response = request.followRedirects(false).withParameter("username", "Vincent").post("/login");
        response.assertHasStatusCode(303);
    }

    @Test
    public void bindingDynamicRequestParametersToPath() throws IOException {
        response = request.get("/hello/Vincent");
        response.assertHasStatusCode(200);
        response.assertHasContentType("text/html");
        response.assertContentEqualTo("<html><body><h3>Hello, Vincent</h3></body></html>");
    }

    @Test
    public void requestingAnUndefinedRoute() throws IOException {
        response = request.get("/nowhere");
        response.assertHasStatusCode(404);
    }
}