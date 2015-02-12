package examples.ssl;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;

public class SSLTest {

    SSLExample ssl = new SSLExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).secure(true);
    HttpResponse response;

    @Before
    public void startServer() throws IOException, GeneralSecurityException {
        ssl.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void connectingSecurely() throws IOException {
        response = request.get("/");
        assertThat(response).hasBodyText("You are on a secure channel");
    }
}