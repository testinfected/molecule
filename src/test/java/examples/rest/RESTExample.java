package examples.rest;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.TextBody;
import com.vtence.molecule.middlewares.HttpMethodOverride;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 *     This example demonstrates RESTful routing. It shows how to define routes, map routes to HTTP verbs
 *     and use route patterns containing named parameters.
 * </p>
 * <p>
 *     We model a simple music library, with routes to query albums, create new albums,
 *     update existing albums and finally delete albums. For this simple example we serve plain text responses.
 * </p>
 * <p>
 *     We use the {@link HttpMethodOverride} middleware to support PUT and DELETE methods
 *     using a simple POST with a special <code>_method</code> request parameter.
 * </p>
 */
public class RESTExample {

    // Our in-memory music library
    private final Map<Integer, Album> albums = new TreeMap<>();
    // A simple sequence for generating new albums identifiers
    private final Sequence sequence = new Sequence();

    public void run(WebServer server) throws IOException {
        // Support HTTP method override via the _method request parameter
        // PUT and DELETE can be done using a POST providing there is a _method
        // parameter that describes the actual verb
        server.add(new HttpMethodOverride())
              .start((new DynamicRoutes() {{
                  // GET to /albums returns the entire list of albums
                  get("/albums").to(Application.of(request -> {
                      // We server a simple plain text response
                      TextBody body = new TextBody();
                      for (int id : albums.keySet()) {
                          Album album = albums.get(id);
                          body.append(String.format("%d: %s\n", id, album.info()));
                      }
                      if (body.text().isEmpty()) {
                          body.append("Your music library is empty");
                      }
                      return Response.ok()
                                     .done(body);
                  }));

                  // POST to /albums creates a new album
                  post("/albums").to(Application.of(request -> {
                      int id = sequence.next();
                      Album album = new Album(request.parameter("title"), request.parameter("artist"));
                      albums.put(id, album);
                      return Response.of(201)
                                     .done(album.info());
                  }));

                  // GET to /albums/<id> fetches an existing album
                  get("/albums/:id").to(Application.of(request -> {
                      int id = Integer.parseInt(request.parameter("id"));
                      if (albums.containsKey(id)) {
                          Album album = albums.get(id);
                          return Response.ok()
                                         .done(album.info());
                      } else {
                          return Response.of(404)
                                         .done();
                      }
                  }));

                  // PUT to /albums/<id> updates an existing album
                  // It can be done with either a PUT or a POST with parameter _method=PUT
                  put("/albums/:id").to(Application.of(request -> {
                      int id = Integer.parseInt(request.parameter("id"));
                      Album album = albums.get(id);
                      if (album != null) {
                          String title = request.parameter("title");
                          if (title != null) album.title = title;
                          String artist = request.parameter("artist");
                          if (artist != null) album.artist = artist;
                          return Response.ok()
                                         .done(album.info());
                      } else {
                          return Response.of(404)
                                         .done();
                      }
                  }));

                  // DELETE to /albums/<id> deletes an existing album
                  // It can be done with either a DELETE or a POST with parameter _method=DELETE
                  delete("/albums/:id").to(Application.of(request -> {
                      int id = Integer.parseInt(request.parameter("id"));
                      Album album = albums.remove(id);
                      if (album != null) {
                          return Response.ok()
                                         .done(album.info());
                      } else {
                          return Response.of(404)
                                         .done();
                      }
                  }));
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
