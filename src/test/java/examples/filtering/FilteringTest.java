package examples.filtering;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class FilteringTest {

    FilteringExample filters = new FilteringExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        filters.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void filteringAccessToContent() throws IOException {
        response = request.get("/private/area?username=admin&password=admin");
        response.assertOK();
        response.assertHasContent("Hello, admin!");
    }

    @Test
    public void haltingRequestProcessingWhenFiltering() throws IOException {
        response = request.get("/private/area?username=admin&password=invalid");
        response.assertHasStatusCode(401);
        response.assertHasContent("Get away!");
    }

    @Test
    public void filteringOnlyMatchingRequests() throws IOException {
        response = request.get("/hello");
        response.assertOK();
        response.assertHasContent("Welcome, Guest!");
    }
}
