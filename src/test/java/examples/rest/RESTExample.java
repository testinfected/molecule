package examples.rest;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.TextBody;
import com.vtence.molecule.middlewares.Failsafe;
import com.vtence.molecule.middlewares.HttpMethodOverride;
import com.vtence.molecule.middlewares.MiddlewareStack;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

import static com.vtence.molecule.middlewares.Router.draw;

public class RESTExample {

    public void run(Server server) throws IOException {
        server.run(new MiddlewareStack() {{
            // Capture internal server errors and display a 500 page
            use(new Failsafe());
            // Support HTTP method override via the _method request parameter
            use(new HttpMethodOverride());

            final Map<Integer, Album> albums = new TreeMap<Integer, Album>();
            final Sequence sequence = new Sequence();

            run(draw(new DynamicRoutes() {{
                get("/albums").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        TextBody body = new TextBody();
                        for (int id : albums.keySet()) {
                            Album album = albums.get(id);
                            body.append(String.format("%d: %s\n", id, album.info()));
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
        }});
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
        // By default, server will run on a random available port...
        SimpleServer server = new SimpleServer();
        new RESTExample().run(server);
        System.out.println("Running on http://" + InetAddress.getLocalHost().getHostName() + ":" + server.port());
    }
}
