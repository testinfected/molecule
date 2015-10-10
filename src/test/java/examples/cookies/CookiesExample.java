package examples.cookies;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;

/**
 * <p>
 *     In this example we demonstrate reading and writing cookies. We use an example adapted from
 *     RFC 2695 (HTTP State Management mechanism - section 4).
 * </p>
 * <p>
 *     We use the {@link Cookies} middleware to enable cookies support. This middleware creates a {@link CookieJar}
 *     and makes it available as a request attribute.
 *     <br>
 *     The cookie jar is initially filled with client cookies.
 *     After response processing, only new, modified or expired cookies in the jar are written back to the client.
 * </p>
 */
public class CookiesExample {

    public void run(WebServer server) throws IOException {
        // Enable cookies with the Cookies middleware. It creates a CookieJar, populates it with client cookies
        // and makes it available a request attribute.
        server.add(new Cookies())
              // We create a simple set of routes to demonstrate various operations on cookies.
              .start(new DynamicRoutes() {{

                  // A GET to / will read the 'customer' client cookie
                  // This will not write any cookie back to client, since we're only reading from the jar
                  get("/").to((request, response) -> {
                      CookieJar cookies = CookieJar.get(request);
                      // Read 'customer' cookie
                      Cookie customer = cookies.get("customer");
                      response.done("Welcome, " + valueOf(customer));
                  });

                  // A GET to /weapon will send back the 'weapon' cookie for path /ammo to Will E. Coyote,
                  // our client. This cookie if valid for the duration of the browser session.
                  get("/weapon").to((request, response) -> {
                      CookieJar cookies = CookieJar.get(request);
                      // Will E. Coyote acquires a rocket launcher
                      // Send back a 'weapon' session cookie for path /ammo
                      cookies.add("weapon", "rocket launcher").path("/ammo");
                      response.done();
                  });

                  // A GET to /ammo sends a 'ammo' cookie for path /ammo which is set to expire after 30s.
                  // It the cookie already exists, it is refreshed.
                  get("/ammo").to((request, response) -> {
                      CookieJar cookies = CookieJar.get(request);
                      // Will. E. Coyote purchases a riding rocket
                      // Set or refresh the 'ammo' cookie with a max age of 30s
                      cookies.add("ammo", "riding rocket").path("/ammo").maxAge(30);
                      response.done();
                  });

                  // A GET to /backfire will expire any existing 'ammo' cookie
                  get("/backfire").to((request, response) -> {
                      CookieJar cookies = CookieJar.get(request);
                      // Riding rocket backfires and explodes
                      // Expire 'weapon' cookie
                      cookies.discard("weapon").path("/ammo");
                      response.done();
                  });
              }});
    }

    private String valueOf(Cookie cookie) {
        return cookie != null ? cookie.value() : null;
    }

    public static void main(String[] args) throws IOException {
        CookiesExample example = new CookiesExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
