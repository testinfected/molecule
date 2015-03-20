package examples.locale;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class LocaleNegotiationTest {

    LocaleNegotiationExample example = new LocaleNegotiationExample("en", "en_US", "fr");
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
    public void fallingBackToTheDefaultLanguage() throws IOException {
        Locale.setDefault(Locale.US);
        response = request.header("Accept-Language", "es-ES").send();
        assertThat(response).hasBodyText(containsString("The best match is: en_US"));
    }
}