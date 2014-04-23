package examples.simple;

import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SimpleTest {

    SimpleServer server = new SimpleServer(9999);

    HttpRequest request = new HttpRequest().onPort(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        new SimpleExample().run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void usesSpecifiedResponseEncoding() throws IOException {
        response = request.get("/pangram?encoding=utf-8");
        response.assertContentIsEncodedAs("utf-8");
    }
}
