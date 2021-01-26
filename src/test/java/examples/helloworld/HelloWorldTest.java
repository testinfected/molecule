package examples.helloworld;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class HelloWorldTest {

    HelloWorldExample helloWorld = new HelloWorldExample();
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        helloWorld.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void respondingWithHelloWorld() throws Exception {
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody("Hello, World!");
    }
}