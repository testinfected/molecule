package examples.helloworld;

import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HelloWorldTest {

    HelloWorldExample server = new HelloWorldExample(8080);

    @Before
    public void startServer() throws IOException {
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void helloWorldExample() throws IOException {
        HttpResponse response = new HttpRequest().onPort(8080).get("/");
        response.assertHasContent("Hello, World");
    }
}
