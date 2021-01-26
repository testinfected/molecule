package examples.simple;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static com.vtence.molecule.testing.http.HttpResponseThat.contentEncodedAs;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.containsString;

public class SimpleTest {

    SimpleExample example = new SimpleExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder();

    @Before
    public void startServer() throws IOException {
        example.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void specifyingResponseOutputEncoding() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("?encoding=utf-8"))
                                          .GET().build(),
                                   ofString());

        assertThat(response)
                .has(contentEncodedAs(StandardCharsets.UTF_8))
                .hasBody(containsString("Les naïfs ægithales hâtifs"));
    }

    @Test
    public void causingTheApplicationToCrashAndRenderA500Page() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("?encoding=not-supported"))
                                          .GET().build(),
                                   ofString());
        assertThat(response).hasStatusCode(500)
                            .hasBody(containsString("java.nio.charset.UnsupportedCharsetException"));
    }
}