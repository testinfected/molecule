package examples.middleware;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.lang.String.valueOf;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class CustomMiddlewareTest {

    CustomMiddlewareExample middlewares = new CustomMiddlewareExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        middlewares.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void shortCircuitingRequestProcessing() throws Exception {
        request.header("User-Agent", "MSIE");
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasStatusCode(303);
    }

    @Test
    public void alteringResponseAfterProcessing() throws Exception {
        request.header("User-Agent", "Chrome");
        var response = client.send(request.GET().build(), ofString());

        String expected = "<html><body>Hello, World</body></html>";

        assertThat(response).hasContentType("text/html")
                            .hasBody(expected)
                            .hasHeader("Content-Length", valueOf(expected.length()));
    }
}