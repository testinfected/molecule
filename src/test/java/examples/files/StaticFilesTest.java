package examples.files;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;

public class StaticFilesTest {

    StaticFilesExample files = new StaticFilesExample(Logging.off());
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

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
        response.assertHasContent(containsString("<p class=\"hello\"></p>"));
    }

    @Test
    public void servingAnStaticFile() throws IOException {
        response = request.get("/js/fox.js");
        response.assertOK();
        response.assertHasContentType("application/javascript");
        response.assertHasContent(containsString("The quick brown fox jumps over the lazy dog"));
    }
}
