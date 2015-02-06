package examples.files;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.http.DeprecatedHttpRequest;
import com.vtence.molecule.support.http.DeprecatedHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;

public class StaticFilesTest {

    StaticFilesExample files = new StaticFilesExample(Logging.off());
    WebServer server = WebServer.create(9999);

    DeprecatedHttpRequest request = new DeprecatedHttpRequest(9999);
    DeprecatedHttpResponse response;

    @Before
    public void startServer() throws IOException {
        files.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void servingTheIndexFile() throws IOException {
        response = request.get("/");
        response.assertOK();
        response.assertHasContentType("text/html");
        response.assertContent(containsString("<p class=\"fox\"></p>"));
    }

    @Test
    public void servingAStaticFile() throws IOException {
        response = request.get("/js/fox.js");
        response.assertOK();
        response.assertHasContentType("application/javascript");
        response.assertContent(containsString("The quick brown fox jumps over the lazy dog"));
    }
}