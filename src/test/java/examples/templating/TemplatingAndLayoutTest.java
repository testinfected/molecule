package examples.templating;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.containsString;

public class TemplatingAndLayoutTest {

    TemplatingAndLayoutExample templating = new TemplatingAndLayoutExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder();

    @Before
    public void startServer() throws IOException {
        templating.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void renderingAnHtmlTemplateUsingMustache() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/hello?name=Frodo"))
                                          .GET().build(),
                                   ofString());
        assertThat(response).isOK()
                            .hasContentType("text/html; charset=utf-8")
                            .hasBody(containsString("<p>Hello, Frodo!</p>"));
    }

    @Test
    public void applyingACommonLayoutToASetOfPages() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/hello"))
                                          .GET().build(),
                                   ofString());
        assertThat(response).isOK()
                            .hasBody(containsString("<title>Layout - Hello World"))
                            .hasBody(containsString("<h1>A simple page</h1>"))
                            .hasBody(containsString("<meta name=\"description\" content=\"Hello World\">"))
                            .hasBody(containsString("<p>Hello, World!</p>"));
    }

    @Test
    public void skippingDecorationWhenResponseStatusIsNotOk() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/nothing"))
                                          .GET().build(),
                                   ofString());
        assertThat(response).hasStatusCode(404)
                            .hasBody("Not found: /nothing");
    }
}