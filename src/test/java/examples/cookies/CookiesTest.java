package examples.cookies;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class CookiesTest {

    CookiesExample example = new CookiesExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        example.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void readingClientCookies() throws IOException {
        response = request.cookie("profile", "wine lover")
                          .cookie("location", "quebec").send();
        assertThat(response).hasBodyText("profile: wine lover, location: quebec");
    }
}