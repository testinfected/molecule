package examples.rest;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.TextBody;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.HttpMethodOverride;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class RESTExample {

    public void run(WebServer server) throws IOException {
        // Support HTTP method override via the _method request parameter
        server.add(new HttpMethodOverride());

        final Map<Integer, Album> albums = new TreeMap<Integer, Album>();
        final Sequence sequence = new Sequence();

        server.start((new DynamicRoutes() {{
            get("/albums").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    TextBody body = new TextBody();
                    for (int id : albums.keySet()) {
                        Album album = albums.get(id);
                        body.append(String.format("%d: %s\n", id, album.info()));
                    }
                    if (body.text().isEmpty()) {
                        body.append("Your music library is empty");
                    }
                    response.body(body);
                }
            });

            post("/albums").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    int id = sequence.next();
                    Album album = new Album(request.parameter("title"), request.parameter("artist"));
                    albums.put(id, album);
                    response.statusCode(201);
                    response.body(album.info());
                }
            });

            get("/albums/:id").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    int id = Integer.parseInt(request.parameter("id"));
                    if (albums.containsKey(id)) {
                        Album album = albums.get(id);
                        response.body(album.info());
                    } else {
                        response.statusCode(404);
                        response.body("No such album");
                    }
                }
            });

            // Access with either a PUT or a POST with _method=PUT
            put("/albums/:id").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    int id = Integer.parseInt(request.parameter("id"));
                    Album album = albums.get(id);
                    if (album != null) {
                        String title = request.parameter("title");
                        if (title != null) album.title = title;
                        String artist = request.parameter("artist");
                        if (artist != null) album.artist = artist;
                        response.body(album.info());
                    } else {
                        response.statusCode(404);
                        response.body("No such album");
                    }
                }
            });

            // Access with either a DELETE or a POST with _method=DELETE
            delete("/albums/:id").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    int id = Integer.parseInt(request.parameter("id"));
                    Album album = albums.remove(id);
                    if (album != null) {
                        response.body(album.info());
                    } else {
                        response.statusCode(404);
                        response.body("No such album");
                    }
                }
            });
        }}));
    }

    public static class Sequence {
        private int next = 1;

        public int next() {
            return next++;
        }
    }

    public static class Album {
        public String title;
        public String artist;

        public Album(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }

        public String info() {
            return String.format("Title: %s, Artist: %s", title, artist);
        }
    }

    public static void main(String[] args) throws IOException {
        // Run the default web server
        WebServer webServer = WebServer.create();
        RESTExample example = new RESTExample();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/albums");
    }
}