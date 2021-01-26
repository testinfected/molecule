package examples.flash;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.Form;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.emptyString;

public class FlashTest {

    FlashExample flash = new FlashExample();

    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        flash.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void retrievingAFlashNoticeInTheNextRequest() throws Exception {
        var form = Form.urlEncoded().addField("email", "james@gmail.com");
        var initial = client.send(request.copy()
                                         .uri(server.uri().resolve("/accounts"))
                                         .header("Content-Type", form.contentType())
                                         .POST(form)
                                         .build(), ofString());

        String redirection = initial.headers().firstValue("Location").orElseThrow();
        var next = client.send(request.copy().uri(server.uri().resolve(redirection))
                                      .build(), ofString());

        assertThat(next).hasBody("Account 'james@gmail.com' successfully created");
    }

    @Test
    public void flashEntriesDoNotSurviveTheNextRequest() throws Exception {
        var form = Form.urlEncoded().addField("email", "james@gmail.com");
        var initial = client.send(request.copy()
                                         .uri(server.uri().resolve("/accounts"))
                                         .header("Content-Type", form.contentType())
                                         .POST(form)
                                         .build(), ofString());

        String redirection = initial.headers().firstValue("Location").orElseThrow();
        client.send(request.copy().uri(server.uri().resolve(redirection))
                           .build(), ofString());

        // play again
        var again = client.send(request.copy().uri(server.uri().resolve(redirection))
                                       .build(), ofString());

        assertThat(again).hasBody(emptyString());
    }

    @Test
    public void usingTheFlashToRedirectWithErrors() throws Exception {
        var initial = client.send(request.copy()
                                         .uri(server.uri().resolve("/accounts"))
                                         .POST(noBody())
                                         .build(), ofString());

        String redirection = initial.headers().firstValue("Location").orElseThrow();
        var next = client.send(request.copy().uri(server.uri().resolve(redirection))
                                      .build(), ofString());

        assertThat(next).hasBody("An email is required");
    }
}
