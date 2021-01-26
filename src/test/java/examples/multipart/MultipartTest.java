package examples.multipart;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.ResourceLocator;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.MultipartForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.Matchers.containsString;

public class MultipartTest {

    MultipartExample upload = new MultipartExample();
    WebServer server = WebServer.create(9999);
    ResourceLocator resources = ResourceLocator.onClasspath();

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        upload.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void submittingTextParameters() throws Exception {
        MultipartForm form = Form.multipart().addField("email", "help@evil.com");

        var response = client.send(request.uri(server.uri().resolve("/profile"))
                                          .header("Content-Type", form.contentType())
                                          .POST(form)
                                          .build(), ofString());

        assertThat(response).isOK()
                            .hasBody(containsString("email: help@evil.com"));
    }

    @Test
    public void uploadingATextFile() throws Exception {
        File biography = resources.locate("examples/upload/evil.txt");
        MultipartForm form = Form.multipart().addTextFile("biography", biography);

        var response = client.send(request.uri(server.uri().resolve("/profile"))
                                          .header("Content-Type", form.contentType())
                                          .POST(form)
                                          .build(), ofString());

        assertThat(response).isOK()
                            .hasBody(containsString("biography: I'm an evil minion!"));
    }

    @Test
    public void uploadingAnEncodedTextFile() throws Exception {
        File biography = resources.locate("examples/upload/mechant.txt");
        MultipartForm form = Form.multipart().addTextFile("biography", biography, "text/plain; charset=utf-16");

        var response = client.send(request.uri(server.uri().resolve("/profile"))
                                          .header("Content-Type", form.contentType())
                                          .POST(form)
                                          .build(), ofString());

        assertThat(response).isOK()
                            .hasBody(containsString("biography: Je suis un m\u00E9chant minion"));
    }

    @Test
    public void uploadingABinaryFile() throws Exception {
        File avatar = locateOnClasspath("examples/upload/evil.png");
        MultipartForm form = Form.multipart().addBinaryFile("avatar", avatar);

        var response = client.send(request.uri(server.uri().resolve("/profile"))
                                          .header("Content-Type", form.contentType())
                                          .POST(form)
                                          .build(), ofString());

        assertThat(response).isOK()
                            .hasBody(containsString("avatar: evil.png (image/png) - 32195 bytes"));
    }
}
