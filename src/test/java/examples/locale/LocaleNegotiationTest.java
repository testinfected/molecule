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

    LocaleNegotiationExample example = new LocaleNegotiationExample("en", "en-US", "fr");
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    Locale originalDefault = Locale.getDefault();

    @Before
    public void startServer() throws IOException {
        Locale.setDefault(Locale.US);
        example.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
        Locale.setDefault(originalDefault);
    }

    @Test
    public void selectingTheBestSupportedLanguage() throws IOException {
        response = request.header("Accept-Language", "en; q=0.8, fr").send();
        assertThat(response).hasBodyText(containsString("The best match is: fr"));
    }

    @Test
    public void fallingBackToTheDefaultLanguage() throws IOException {
        response = request.header("Accept-Language", "es-ES").send();
        assertThat(response).hasBodyText(containsString("The best match is: en-US"));
    }
}