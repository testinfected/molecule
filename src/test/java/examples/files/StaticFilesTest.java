package examples.files;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.test.HttpRequest;
import com.vtence.molecule.test.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.test.HttpResponseAssert.assertThat;
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
        assertThat(response).isOK()
                            .hasContentType("text/html")
                            .hasBodyText(containsString("<p class=\"fox\"></p>"));
    }

    @Test
    public void servingAStaticFile() throws IOException {
        response = request.get("/js/fox.js");
        assertThat(response).isOK()
                            .hasContentType("application/javascript")
                            .hasBodyText(containsString("The quick brown fox jumps over the lazy dog"));
    }
}