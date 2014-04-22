package examples.routing;

import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import examples.rest.RESTExample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RoutingTest {

    RoutingExample server = new RoutingExample(8080);
    HttpRequest request = new HttpRequest().onPort(8080);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void RoutingExample() throws IOException {
        response = request.get("/");
        response.assertHasStatusCode(200);
        response.assertHasContent("Welcome!");

        response = request.but().get("/hello");
        response.assertHasStatusCode(200);
        response.assertHasContent("Hello, World");

        // Default behavior is to follow redirects
        response = request.but().withParameter("username", "Vincent").post("/login");
        response.assertHasStatusCode(200);
        response.assertHasContentType("text/html");
        response.assertHasContent("<html><body><h3>Hello, Vincent</h3></body></html>");
    }
}