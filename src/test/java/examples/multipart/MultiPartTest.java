package examples.multipart;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.ResourceLocator;
import com.vtence.molecule.testing.FormData;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;

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
    public void submittingFormDataParameters() throws IOException {
        FormData form = new FormData().addField("say", "Hello")
                                      .addField("to", "world");
        response = request.body(form).post("/greeting");

        assertThat(response).isOK()
                            .hasBodyText("Hello world");
    }

    @Test
    public void uploadingATextFile() throws IOException {
        File biography = resources.locate("examples/upload/evil.txt");
        FormData form = new FormData().addTextFile("biography", biography);
        response = request.body(form).post("/biography");

        assertThat(response).isOK()
                            .hasBodyText("I'm an evil minion!");
    }

    @Test
    public void uploadingABinaryFile() throws IOException {
        File avatar = locateOnClasspath("examples/upload/evil.png");
        FormData form = new FormData().addBinaryFile("avatar", avatar);
        response = request.body(form).post("/avatar");

        assertThat(response).isOK()
                            .hasContentType("image/png")
                            .hasHeader("X-File-Name", avatar.getName())
                            .hasBodySize(32195);
    }
}