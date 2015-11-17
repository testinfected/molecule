package examples.multiapps;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class MultiAppsTest {

    MultiAppsExample multiApps = new MultiAppsExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        multiApps.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void dispatchingRequestsToMultipleApplicationsDependingOnThePath() throws IOException {
        response = request.get("/foo/quux");
        assertThat(response).isOK().hasBodyText("/foo at /quux (/foo/quux)");

        response = request.get("/foo/bar/quux");
        assertThat(response).isOK().hasBodyText("/foo/bar at /quux (/foo/bar/quux)");

        response = request.get("/baz");
        assertThat(response).isOK().hasBodyText("/baz at / (/baz)");
    }

    @Test
    public void gettingA404OnRequestToUnmappedPath() throws IOException {
        response = request.get("/quux");
        assertThat(response).hasStatusCode(404)
                            .hasBodyText("Not found: /quux");
    }
}
