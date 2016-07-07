package examples.auth;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class BasicAuthTest {

    BasicAuthExample auth = new BasicAuthExample("WallyWorld");
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        auth.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void receivingAChallengeWhenNoCredentialsAreSpecified() throws IOException {
        response = request.get("/");

        assertThat(response).hasStatusCode(401)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .hasContentType("text/plain")
                            .isEmpty();
    }
}
