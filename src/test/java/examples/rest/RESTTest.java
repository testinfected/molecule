package examples.rest;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.UrlEncodedForm;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class RESTTest {

    RESTExample rest = new RESTExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        rest.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void managingAlbumResources() throws IOException {
        response = request.but().content(new UrlEncodedForm().addField("title", "My Favorite Things")
                                                             .addField("artist", "John Coltrane"))
                                .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but().get("/albums/1");
        assertThat(response).isOK()
                            .hasBodyText("Title: My Favorite Things, Artist: John Coltrane");

        response = request.but().content(new UrlEncodedForm().addField("title", "Blue Train")
                                                             .addField("artist", "John Coltrane"))
                                .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but().get("/albums");
        assertThat(response).isOK()
                            .hasBodyText("1: Title: My Favorite Things, Artist: John Coltrane\n" +
                                         "2: Title: Blue Train, Artist: John Coltrane\n");

        response = request.but().content(new UrlEncodedForm().addField("title", "Kind of Blue")
                                                             .addField("artist", "Miles Davis"))
                                .put("/albums/2");
        assertThat(response).isOK()
                            .hasBodyText("Title: Kind of Blue, Artist: Miles Davis");

        response = request.but().delete("/albums/1");
        assertThat(response).isOK();

        response = request.but().get("/albums");
        assertThat(response).isOK()
                            .hasBodyText("2: Title: Kind of Blue, Artist: Miles Davis\n");
    }

    @Test
    public void makingAPostActLikeAnUpdateOrDelete() throws IOException {
        response = request.but()
                          .content(new UrlEncodedForm().addField("title", "My Favorite Things")
                                                       .addField("artist", "John Coltrane"))
                          .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but()
                          .content(new UrlEncodedForm().addField("_method", "PUT")
                                                       .addField("title", "Kind of Blue")
                                                       .addField("artist", "Miles Davis"))
                          .post("/albums/1");
        assertThat(response).isOK();

        response = request.but().get("/albums/1");
        assertThat(response).isOK()
                            .hasBodyText("Title: Kind of Blue, Artist: Miles Davis");

        response = request.but().content(new UrlEncodedForm().addField("_method", "DELETE"))
                                .post("/albums/1");
        assertThat(response).isOK();

        response = request.but().get("/albums");
        assertThat(response).isOK()
                            .hasBodyText("Your music library is empty");
    }

    @Test
    public void askingForAMissingAlbum() throws IOException {
        response = request.get("/albums/9999");
        assertThat(response).hasStatusCode(404);
    }
}