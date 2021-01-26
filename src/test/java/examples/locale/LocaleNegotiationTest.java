package examples.locale;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Locale;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.containsString;

public class LocaleNegotiationTest {

    LocaleNegotiationExample example = new LocaleNegotiationExample("en", "en-US", "fr", "da-DK");
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

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
    public void selectingTheBestSupportedLanguage() throws Exception {
        request.header("Accept-Language", "en; q=0.8, fr");
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody(containsString("The best match is: fr\n"));
    }

    @Test
    public void fallingBackToTheDefaultLanguage() throws Exception {
        request.header("Accept-Language", "es-ES");
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody(containsString("The best match is: en-US\n"));
    }

    @Test
    public void fallingBackToAMoreGeneralLanguage() throws Exception {
        request.header("Accept-Language", "en-GB");
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody(containsString("The best match is: en\n"));
    }

    @Test
    public void usingACountrySpecificLanguageWhenTheGeneralOneIsNotSupported() throws Exception {
        request.header("Accept-Language", "da");
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody(containsString("The best match is: da-DK\n"));
    }
}