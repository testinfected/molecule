package examples.helloworld;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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
        response.assertContentEqualTo("Hello, World!");
    }
}