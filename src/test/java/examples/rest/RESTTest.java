package examples.rest;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.http.HttpRequest;
import com.vtence.molecule.support.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RESTTest {

    RESTExample rest = new RESTExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).withTimeout(50000);
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
        response = request.but()
                          .withParameter("title", "My Favorite Things")
                          .withParameter("artist", "John Coltrane")
                          .post("/albums");
        response.assertHasStatusCode(201);

        response = request.but().get("/albums/1");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo("Title: My Favorite Things, Artist: John Coltrane");

        response = request.but()
                          .withParameter("title", "Blue Train")
                          .withParameter("artist", "John Coltrane")
                          .post("/albums");
        response.assertHasStatusCode(201);

        response = request.but().get("/albums");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo(
                "1: Title: My Favorite Things, Artist: John Coltrane\n" +
                        "2: Title: Blue Train, Artist: John Coltrane\n"
        );

        // HtmUnit requires us to pass PUT parameters as part of the query string
        response = request.but().put("/albums/2?title=Kind of Blue&artist=Miles Davis");
        response.assertHasStatusCode(200);

        response = request.but().delete("/albums/1");
        response.assertHasStatusCode(200);

        response = request.but().get("/albums");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo("2: Title: Kind of Blue, Artist: Miles Davis\n");
    }

    @Test
    public void makingAPostActLikeAnUpdateOrDelete() throws IOException {
        response = request.but()
                          .withParameter("title", "My Favorite Things")
                          .withParameter("artist", "John Coltrane")
                          .post("/albums");
        response.assertHasStatusCode(201);

        response = request.but()
                          .withParameter("_method", "PUT")
                          .withParameter("title", "Kind of Blue")
                          .withParameter("artist", "Miles Davis")
                          .post("/albums/1");
        response.assertHasStatusCode(200);

        response = request.but().get("/albums/1");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo("Title: Kind of Blue, Artist: Miles Davis");

        response = request.but().withParameter("_method", "DELETE").post("/albums/1");
        response.assertHasStatusCode(200);

        response = request.but().get("/albums");
        response.assertHasStatusCode(200);
        response.assertContentEqualTo("Your music library is empty");
    }
}