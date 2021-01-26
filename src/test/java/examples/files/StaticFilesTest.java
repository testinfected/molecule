package examples.files;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.containsString;

public class StaticFilesTest {

    StaticFilesExample files = new StaticFilesExample(Logging.off());
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newBuilder().build();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        files.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void servingTheIndexFile() throws Exception {
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).isOK()
                            .hasContentType("text/html")
                            .hasBody(containsString("<p class=\"fox\"></p>"));
    }

    @Test
    public void servingAStaticFile() throws Exception {
        request.uri(server.uri().resolve("/js/fox.js"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).isOK()
                            .hasContentType("application/javascript")
                            .hasBody(containsString("The quick brown fox jumps over the lazy dog"));
    }
}