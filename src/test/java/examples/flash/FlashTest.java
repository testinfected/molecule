package examples.flash;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class FlashTest {

    FlashExample flash = new FlashExample();

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    WebServer server = WebServer.create(9999);

    @Before
    public void startServer() throws IOException {
        flash.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void retrievingAFlashNoticeInTheNextRequest() throws IOException {
        response = request.but()
                          .content(Form.urlEncoded().addField("email", "james@gmail.com"))
                          .post("/accounts");

        String redirection = response.header("Location");
        String sessionId = response.cookie("molecule.session").getValue();
        response = request.but()
                          .cookie("molecule.session", sessionId)
                          .get(redirection);

        assertThat(response).hasBodyText("Account 'james@gmail.com' successfully created");
    }

    @Test
    public void flashEntriesDoNotSurviveTheNextRequest() throws IOException {
        response = request.but()
                          .content(Form.urlEncoded().addField("email", "james@gmail.com"))
                          .post("/accounts");

        String redirection = response.header("Location");
        String sessionId = response.cookie("molecule.session").getValue();
        response = request.but()
                          .cookie("molecule.session", sessionId)
                          .get(redirection);

        // Session id might have been updated
        sessionId = response.cookie("molecule.session") != null ? response.cookie("molecule.session").getValue() :
                sessionId;
        // play again
        response = request.but()
                          .cookie("molecule.session", sessionId)
                          .get(redirection);

        assertThat(response).hasBodyText("");
    }

    @Test
    public void usingTheFlashToRedirectWithErrors() throws IOException {
        response = request.but()
                          .post("/accounts");

        String redirection = response.header("Location");
        String sessionId = response.cookie("molecule.session").getValue();
        response = request.but()
                          .cookie("molecule.session", sessionId)
                          .get(redirection);

        assertThat(response).hasBodyText("An email is required");
    }
}
