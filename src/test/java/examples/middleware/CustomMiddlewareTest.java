package examples.middleware;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static java.lang.String.valueOf;

public class CustomMiddlewareTest {

    CustomMiddlewareExample middlewares = new CustomMiddlewareExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).followRedirects(false);
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
        response = request.withHeader("User-Agent", "MSIE").get("/");
        response.assertHasStatusCode(303);
    }

    @Test
    public void alteringResponseAfterProcessing() throws IOException {
        response = request.withHeader("User-Agent", "Chrome").get("/");
        response.assertHasContentType("text/html");
        String content = "<html><body>Hello, World</body></html>";
        response.assertContentEqualTo(content);
        response.assertHasHeader("Content-Length", valueOf(content.length()));
    }
}
