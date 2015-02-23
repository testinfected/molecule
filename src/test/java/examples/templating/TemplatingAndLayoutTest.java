package examples.templating;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
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
        assertThat(response).isOK()
                            .hasContentType("text/html; charset=utf-8")
                            .hasBodyText(containsString("<p>Hello, Frodo!</p>"));
    }

    @Test
    public void applyingACommonLayoutToASetOfPages() throws IOException {
        response = request.get("/hello");
        assertThat(response).isOK()
                            .hasBodyText(containsString("<title>Layout - Hello World"))
                            .hasBodyText(containsString("<h1>A simple page</h1>"))
                            .hasBodyText(containsString("<meta name=\"description\" content=\"Hello World\">"))
                            .hasBodyText(containsString("<p>Hello, World!</p>"));
    }

    @Test
    public void skippingDecorationWhenResponseStatusIsNotOk() throws IOException {
        response = request.get("/not-found");
        assertThat(response).hasStatusCode(404)
                            .hasBodyText("Not found: /not-found");
    }
}