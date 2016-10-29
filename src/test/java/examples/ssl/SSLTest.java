package examples.ssl;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class SSLTest {

    SSLExample ssl = new SSLExample();

    WebServer secureServer = WebServer.create("localhost", 9443);
    WebServer insecureServer = WebServer.create("localhost", 9999);

    HttpRequest sslRequest = new HttpRequest(9443).secure(true);
    HttpRequest insecureRequest = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException, GeneralSecurityException {
        ssl.redirect(insecureServer, secureServer);
        ssl.run(secureServer);
    }

    @After
    public void stopServer() throws IOException {
        secureServer.stop();
        insecureServer.stop();
    }

    @Test
    public void connectingSecurely() throws IOException {
        response = sslRequest.get("/");
        assertThat(response).hasBodyText("You are on a secure channel");
    }

    @Test
    public void redirectingToASecureConnection() throws IOException {
        response = insecureRequest.get("/resource");

        assertThat(response).hasStatusCode(301)
                            .hasHeader("Location", "https://localhost:9443/resource");
    }
}