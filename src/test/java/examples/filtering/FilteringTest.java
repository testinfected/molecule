package examples.filtering;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;

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
    public void authorizingAccessToPrivateContent() throws IOException {
        response = request.get("/private/area?username=admin&password=admin");
        assertThat(response).isOK().hasBodyText("Hello, admin!");
    }

    @Test
    public void preventingAccessToPrivateContent() throws IOException {
        response = request.get("/private/area?username=admin&password=invalid");
        assertThat(response).hasStatusCode(401).hasBodyText("Get away!");
    }

    @Test
    public void givingAccessPublicContent() throws IOException {
        response = request.get("/hello");
        assertThat(response).isOK().hasBodyText("Welcome, Guest!");
    }
}