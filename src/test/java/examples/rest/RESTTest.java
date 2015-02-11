package examples.rest;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.test.HtmlForm;
import com.vtence.molecule.test.HttpRequest;
import com.vtence.molecule.test.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.test.HttpResponseAssert.assertThat;

public class RESTTest {

    RESTExample rest = new RESTExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).charset(Charsets.UTF_8);
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
        response = request.but().body(new HtmlForm().set("title", "My Favorite Things")
                                                    .set("artist", "John Coltrane"))
                                .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but().get("/albums/1");
        assertThat(response).isOK()
                            .hasBodyText("Title: My Favorite Things, Artist: John Coltrane");

        response = request.but().body(new HtmlForm().set("title", "Blue Train")
                                                    .set("artist", "John Coltrane"))
                                .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but().get("/albums");
        assertThat(response).isOK()
                            .hasBodyText("1: Title: My Favorite Things, Artist: John Coltrane\n" +
                                         "2: Title: Blue Train, Artist: John Coltrane\n");

        response = request.but().body(new HtmlForm().set("title", "Kind of Blue")
                                                    .set("artist", "Miles Davis"))
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
                          .body(new HtmlForm().set("title", "My Favorite Things")
                                              .set("artist", "John Coltrane"))
                          .post("/albums");
        assertThat(response).hasStatusCode(201);

        response = request.but()
                          .body(new HtmlForm().set("_method", "PUT")
                                              .set("title", "Kind of Blue")
                                              .set("artist", "Miles Davis"))
                                  .post("/albums/1");
        assertThat(response).isOK();

        response = request.but().get("/albums/1");
        assertThat(response).isOK()
                            .hasBodyText("Title: Kind of Blue, Artist: Miles Davis");

        response = request.but().body(new HtmlForm().set("_method", "DELETE"))
                                .post("/albums/1");
        assertThat(response).isOK();

        response = request.but().get("/albums");
        assertThat(response).isOK()
                            .hasBodyText("Your music library is empty");
    }
}