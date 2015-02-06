package examples.filtering;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.http.DeprecatedHttpRequest;
import com.vtence.molecule.support.http.DeprecatedHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class FilteringTest {

    FilteringExample filters = new FilteringExample();
    WebServer server = WebServer.create(9999);

    DeprecatedHttpRequest request = new DeprecatedHttpRequest(9999);
    DeprecatedHttpResponse response;

    @Before
    public void startServer() throws IOException {
        filters.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void authorizingAccessToPrivateContent() throws IOException {
        response = request.get("/private/area?username=admin&password=admin");
        response.assertOK();
        response.assertContentEqualTo("Hello, admin!");
    }

    @Test
    public void preventingAccessToPrivateContent() throws IOException {
        response = request.get("/private/area?username=admin&password=invalid");
        response.assertHasStatusCode(401);
        response.assertContentEqualTo("Get away!");
    }

    @Test
    public void givingAccessPublicContent() throws IOException {
        response = request.get("/hello");
        response.assertOK();
        response.assertContentEqualTo("Welcome, Guest!");
    }
}