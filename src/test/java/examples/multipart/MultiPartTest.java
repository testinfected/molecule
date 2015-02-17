package examples.multipart;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.FormData;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;

public class MultiPartTest {

    MultiPartExample upload = new MultiPartExample();
    WebServer server = WebServer.create(9999);

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
        response = request.body(new FormData().set("say", "Hello")
                                              .set("to", "world")).post("/");

        assertThat(response).isOK()
                            .hasBodyText("Hello world");
    }
}
