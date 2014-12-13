package examples.ssl;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SSLTest {

    SSLExample ssl = new SSLExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).useSSL();
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
        response.assertContentEqualTo("You are on a secure channel");
    }
}