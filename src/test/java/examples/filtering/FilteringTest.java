package examples.filtering;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class FilteringTest {

    FilteringExample filters = new FilteringExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newBuilder().build();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        filters.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void authorizingAccessToPrivateContent() throws Exception {
        request.uri(server.uri().resolve("/private/area?username=admin&password=admin"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).isOK().hasBody("Hello, admin!");
    }

    @Test
    public void preventingAccessToPrivateContent() throws Exception {
        request.uri(server.uri().resolve("/private/area?username=admin&password=invalid"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasStatusCode(401).hasBody("Get away!");
    }

    @Test
    public void givingAccessPublicContent() throws Exception {
        request.uri(server.uri().resolve("/hello"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).isOK().hasBody("Welcome, Guest!");
    }
}