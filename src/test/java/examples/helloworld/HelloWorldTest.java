package examples.helloworld;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.test.HttpRequest;
import com.vtence.molecule.test.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.test.HttpResponseAssert.assertThat;

public class HelloWorldTest {

    HelloWorldExample helloWorld = new HelloWorldExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        helloWorld.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void respondingWithHelloWorld() throws IOException {
        response = request.get("/");
        assertThat(response).hasBodyText("Hello, World!");
    }
}