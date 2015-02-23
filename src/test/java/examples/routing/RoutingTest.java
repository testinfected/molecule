package examples.routing;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.Form;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;

public class RoutingTest {

    RoutingExample routing = new RoutingExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        routing.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void mappingRoutesToUrls() throws IOException {
        response = request.get("/");
        assertThat(response).isOK()
                            .hasBodyText("Welcome!");
    }

    @Test
    public void restrictingARouteToASpecificVerb() throws IOException {
        response = request.content(Form.urlEncoded().addField("username", "Vincent")).post("/login");
        assertThat(response).hasStatusCode(303);
    }

    @Test
    public void bindingDynamicRequestParametersToPath() throws IOException {
        response = request.get("/hello/Vincent");
        assertThat(response).isOK()
                            .hasContentType("text/html")
                            .hasBodyText("<html><body><h3>Hello, Vincent</h3></body></html>");
    }

    @Test
    public void requestingAnUndefinedRoute() throws IOException {
        response = request.get("/nowhere");
        assertThat(response).hasStatusCode(404);
    }
}