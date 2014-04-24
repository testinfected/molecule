package examples.templating;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;

public class TemplatingTest {

    TemplatingExample templating = new TemplatingExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        templating.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void renderingAnHtmlTemplateUsingMustache() throws IOException {
        response = request.get("/?name=Vincent");
        response.assertHasContentType("text/html; charset=utf-8");
        response.assertHasContent(containsString("<h3>Hello Vincent</h3>"));
    }
}
