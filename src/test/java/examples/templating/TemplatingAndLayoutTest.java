package examples.templating;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;

public class TemplatingAndLayoutTest {

    TemplatingAndLayoutExample templating = new TemplatingAndLayoutExample();
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
        response = request.get("/hello?name=Frodo");
        response.assertOK();
        response.assertHasContentType("text/html; charset=utf-8");
        response.assertContent(containsString("<p>Hello, Frodo!</p>"));
    }

    @Test
    public void applyingACommonLayoutToASetOfPages() throws IOException {
        response = request.get("/hello");
        response.assertOK();
        response.assertContent(containsString("<title>Layout - Hello World"));
        response.assertContent(containsString("<h1>A simple page</h1>"));
        response.assertContent(containsString("<meta name=\"description\" content=\"Hello World\">"));
        response.assertContent(containsString("Hello, World!"));
    }

    @Test
    public void skippingDecorationWhenResponseStatusIsNotOk() throws IOException {
        response = request.get("/not-found");
        response.assertHasStatusCode(404);
        response.assertContentEqualTo("Not found: /not-found");
    }
}
