package examples.simple;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.http.HttpRequest;
import com.vtence.molecule.support.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;

public class SimpleTest {

    SimpleExample basic = new SimpleExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        basic.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void specifyingResponseOutputEncoding() throws IOException {
        response = request.get("/?encoding=utf-8");
        response.assertContentIsEncodedAs("utf-8");
    }

    @Test
    public void causingTheApplicationToCrashAndRenderA500Page() throws IOException {
        response = request.get("/?encoding=not-supported");
        response.assertHasStatusCode(500);
        response.assertContent(containsString("java.nio.charset.UnsupportedCharsetException"));
    }
}