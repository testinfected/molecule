package examples.simple;

import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SimpleTest {

    SimpleExample server = new SimpleExample();

    @Before
    public void startServer() throws IOException {
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void simpleExample() throws IOException {
        // Query the dynamically allocated port
        HttpRequest request = new HttpRequest().onPort(server.port());
        HttpResponse response = request.get("/pangram?encoding=utf-8");
        response.assertContentIsEncodedAs("utf-8");
    }
}
