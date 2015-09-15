package examples.rest;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.TextBody;
import com.vtence.molecule.middlewares.HttpMethodOverride;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class RESTExample {

    public void run(WebServer server) throws IOException {
        // Support HTTP method override via the _method request parameter
        server.add(new HttpMethodOverride());

        final Map<Integer, Album> albums = new TreeMap<>();
        final Sequence sequence = new Sequence();

        server.start((new DynamicRoutes() {{
            get("/albums").to((request, response) -> {
                TextBody body = new TextBody();
                for (int id : albums.keySet()) {
                    Album album = albums.get(id);
                    body.append(String.format("%d: %s\n", id, album.info()));
                }
                if (body.text().isEmpty()) {
                    body.append("Your music library is empty");
                }
                response.done(body);
            });

            post("/albums").to((request, response) -> {
                int id = sequence.next();
                Album album = new Album(request.parameter("title"), request.parameter("artist"));
                albums.put(id, album);
                response.statusCode(201)
                        .done(album.info());
            });

            get("/albums/:id").to((request, response) -> {
                int id = Integer.parseInt(request.parameter("id"));
                if (albums.containsKey(id)) {
                    Album album = albums.get(id);
                    response.done(album.info());
                } else {
                    response.statusCode(404).done();
                }
            });

            // Access with either a PUT or a POST with _method=PUT
            put("/albums/:id").to((request, response) -> {
                int id = Integer.parseInt(request.parameter("id"));
                Album album = albums.get(id);
                if (album != null) {
                    String title = request.parameter("title");
                    if (title != null) album.title = title;
                    String artist = request.parameter("artist");
                    if (artist != null) album.artist = artist;
                    response.done(album.info());
                } else {
                    response.statusCode(404).done();
                }
            });

            // Access with either a DELETE or a POST with _method=DELETE
            delete("/albums/:id").to((request, response) -> {
                int id = Integer.parseInt(request.parameter("id"));
                Album album = albums.remove(id);
                if (album != null) {
                    response.done(album.info());
                } else {
                    response.statusCode(404).done();
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
        RESTExample example = new RESTExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/albums");
    }
}