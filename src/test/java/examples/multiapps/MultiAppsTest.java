package examples.multiapps;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class MultiAppsTest {

    MultiAppsExample multiApps = new MultiAppsExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        multiApps.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void dispatchingRequestsToMultipleApplicationsDependingOnThePath() throws Exception {
        var foo = client.send(request.uri(server.uri().resolve("/foo/quux")).GET().build(), ofString());
        assertThat(foo).isOK().hasBody("/foo at /quux (/foo/quux)");

        var bar = client.send(request.uri(server.uri().resolve("/foo/bar/quux")).GET().build(), ofString());
        assertThat(bar).isOK().hasBody("/foo/bar at /quux (/foo/bar/quux)");

        var baz = client.send(request.uri(server.uri().resolve("/baz")).GET().build(), ofString());
        assertThat(baz).isOK().hasBody("/baz at / (/baz)");
    }

    @Test
    public void gettingA404OnRequestToUnmappedPath() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/quux")).build(), ofString());

        assertThat(response).hasStatusCode(404).hasBody("Not found: /quux");
    }
}
