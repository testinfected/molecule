package examples.simple;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class SimpleTest {

    SimpleExample basic = new SimpleExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        basic.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void specifyingResponseOutputEncoding() throws IOException {
        response = request.get("/?encoding=utf-8");
        assertThat(response).hasContentEncodedAs("utf-8")
                            .hasBodyText(containsString("Les naïfs ægithales hâtifs"));
    }

    @Test
    public void causingTheApplicationToCrashAndRenderA500Page() throws IOException {
        response = request.get("/?encoding=not-supported");
        assertThat(response).hasStatusCode(500)
                            .hasBodyText(containsString("java.nio.charset.UnsupportedCharsetException"));
    }
}