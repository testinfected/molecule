package examples.multipart;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.ResourceLocator;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import com.vtence.molecule.testing.http.MultipartForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MultiPartTest {

    MultiPartExample upload = new MultiPartExample();
    WebServer server = WebServer.create(9999);
    ResourceLocator resources = ResourceLocator.onClasspath();

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        upload.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void submittingTextParameters() throws IOException {
        MultipartForm form = Form.multipart().addField("email", "help@evil.com");
        response = request.content(form).post("/profile");

        assertThat(response).isOK()
                            .hasBodyText(containsString("email: help@evil.com"));
    }

    @Test
    public void uploadingATextFile() throws IOException {
        File biography = resources.locate("examples/upload/evil.txt");
        MultipartForm form = Form.multipart().addTextFile("biography", biography);
        response = request.content(form).post("/profile");

        assertThat(response).isOK()
                            .hasBodyText(containsString("biography: I'm an evil minion!"));
    }

    @Test
    public void uploadingAnEncodedTextFile() throws IOException {
        File biography = resources.locate("examples/upload/mechant.txt");
        MultipartForm form = Form.multipart().addTextFile("biography", biography, "text/plain; charset=utf-16");
        response = request.content(form).post("/profile");

        assertThat(response).isOK()
                            .hasBodyText(containsString("biography: Je suis un méchant minion"));
    }

    @Test
    public void uploadingABinaryFile() throws IOException {
        File avatar = locateOnClasspath("examples/upload/evil.png");
        MultipartForm form = Form.multipart().addBinaryFile("avatar", avatar);
        response = request.content(form).post("/profile");

        assertThat(response).isOK()
                            .hasBodyText(containsString("avatar: evil.png (image/png) - 32195 bytes"));
    }
}