package examples.auth;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.MimeEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.emptyString;

public class BasicAuthTest {

    BasicAuthExample auth = new BasicAuthExample("WallyWorld");
    WebServer server = WebServer.create(9999);
    MimeEncoder base64 = MimeEncoder.inUtf8();

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        auth.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void receivingAChallengeWhenNoCredentialsAreSpecified() throws Exception {
        var response = client.send(request.build(), ofString());

        assertThat(response).hasStatusCode(401)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .hasContentType("text/plain")
                            .hasBody(emptyString());
    }

    @Test
    public void authenticatingWithValidCredentials() throws Exception {
        auth.addUser("Joe", "secret");

        request.header("Authorization", "Basic " + base64.encode("Joe:secret"));
        var response = client.send(request.build(), ofString());

        assertThat(response).isOK()
                            .hasBody("Hello, Joe");
    }

    @Test
    public void receivingANewChallengeWhenCredentialsAreInvalid() throws Exception {
        auth.addUser("Joe", "secret");

        request.header("Authorization", "Basic " + base64.encode("Joe:bad secret"));
        var response = client.send(request.build(), discarding());

        assertThat(response).hasStatusCode(401)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"");
    }
}
