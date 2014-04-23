package examples.helloworld;

import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HelloWorldTest {

    SimpleServer server = new SimpleServer(9999);

    HttpRequest request = new HttpRequest().onPort(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        new HelloWorldExample().run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void respondsWithHelloWorld() throws IOException {
        response = request.get("/");
        response.assertHasContent("Hello, World");
    }
}
