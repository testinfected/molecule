package examples.routing;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.Form;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class RoutingTest {

    RoutingExample routing = new RoutingExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());


    @Before
    public void startServer() throws IOException {
        routing.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void mappingRoutesToUrls() throws Exception {
        var response = client.send(request.GET().build(), ofString());

        assertThat(response).isOK()
                            .hasBody("Welcome!");
    }

    @Test
    public void restrictingARouteToASpecificVerb() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/login"))
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded().addField("username", "Vincent"))
                                          .build(), ofString());

        assertThat(response).hasStatusCode(303);
    }

    @Test
    public void bindingDynamicRequestParametersToPath() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/hello/Vincent"))
                                          .GET()
                                          .build(), ofString());

        assertThat(response).isOK()
                            .hasContentType("text/html")
                            .hasBody("<html><body><h3>Hello, Vincent</h3></body></html>");
    }

    @Test
    public void requestingAnUndefinedRoute() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/nowhere"))
                                          .GET()
                                          .build(), ofString());
        assertThat(response).hasStatusCode(404);
    }
}