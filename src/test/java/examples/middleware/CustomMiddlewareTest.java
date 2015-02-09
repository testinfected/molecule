package examples.middleware;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.test.HttpRequest;
import com.vtence.molecule.test.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.test.HttpResponseAssert.assertThat;
import static java.lang.String.valueOf;

public class CustomMiddlewareTest {

    CustomMiddlewareExample middlewares = new CustomMiddlewareExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        middlewares.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void shortCircuitingRequestProcessing() throws IOException {
        response = request.header("User-Agent", "MSIE").get("/");
        assertThat(response).hasStatusCode(303);
    }

    @Test
    public void alteringResponseAfterProcessing() throws IOException {
        response = request.header("User-Agent", "Chrome").get("/");

        String expected = "<html><body>Hello, World</body></html>";

        assertThat(response).hasContentType("text/html")
                            .hasBodyText(expected)
                            .hasHeader("Content-Length", valueOf(expected.length()));
    }
}