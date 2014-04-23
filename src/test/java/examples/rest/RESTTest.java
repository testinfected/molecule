package examples.rest;

import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RESTTest {

    SimpleServer server = new SimpleServer(9999);

    HttpRequest request = new HttpRequest().onPort(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        new RESTExample().run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void drawsRoutesForManagingAlbums() throws IOException {
        response = request.withParameter("title", "My Favorite Things").withParameter("artist", "John Coltrane").post("/albums");
        response.assertHasStatusCode(201);

        response = request.but().get("/albums/1");
        response.assertHasStatusCode(200);
        response.assertHasContent("Title: My Favorite Things, Artist: John Coltrane");

        response = request.but().withParameter("title", "Blue Train").withParameter("artist", "John Coltrane").post("/albums");
        response.assertHasStatusCode(201);

        response = request.but().get("/albums");
        response.assertHasStatusCode(200);
        response.assertHasContent(
                "1: Title: My Favorite Things, Artist: John Coltrane\n" +
                "2: Title: Blue Train, Artist: John Coltrane\n");

        response = request.but().
                withParameter("_method", "PUT").
                withParameter("title", "Kind of Blue").
                withParameter("artist", "Miles Davis").
                post("/albums/2");
        response.assertHasStatusCode(200);

        response = request.but().withParameter("_method", "DELETE").post("/albums/1");
        response.assertHasStatusCode(200);

        response = request.but().get("/albums");
        response.assertHasStatusCode(200);
        response.assertHasContent("2: Title: Kind of Blue, Artist: Miles Davis\n");
    }
}
