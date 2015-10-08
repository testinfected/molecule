package examples.session;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.session.SecureIdentifierPolicy;
import com.vtence.molecule.session.Session;
import com.vtence.molecule.session.SessionPool;

import java.io.IOException;
import java.time.Clock;

public class SessionExample {

    private static final int FIVE_MINUTES = 300; // in secs
    private static final int THIRTY_MINUTES = 1800; // in secs

    private final Clock clock;

    public SessionExample(Clock clock) {
        this.clock = clock;
    }

    public void run(WebServer server) throws IOException {
        // Create an in-memory session pool which invalidates stale sessions after 30 minutes
        SessionPool sessionPool = new SessionPool(new SecureIdentifierPolicy(), clock).idleTimeout(THIRTY_MINUTES);

              // Enable cookie support
        server.add(new Cookies())
              // Track sessions using transient - a.k.a session - cookies by default
              .add(new CookieSessionTracker(sessionPool))
              .start(new DynamicRoutes() {{
                         map("/").to((request, response) -> {
                             Session session = Session.get(request);
                             String username = session.contains("username") ? session.<String>get("username") : "Guest";
                             response.done("Hello, " + username);
                         });

                         post("/login").to((request, response) -> {
                             String username = request.parameter("username");
                             Session session = Session.get(request);
                             session.put("username", username);

                             // if remember me is checked, make session cookie persistent with a lifetime of 5 minutes
                             boolean rememberMe = request.parameter("remember_me") != null;
                             if (rememberMe) {
                                 session.maxAge(FIVE_MINUTES);
                             }

                             response.redirectTo("/").done();
                         });

                         delete("/logout").to((request, response) -> {
                             Session session = Session.get(request);
                             session.invalidate();
                             response.redirectTo("/").done();
                         });
                     }}
              );
    }

    public static void main(String[] args) throws IOException {
        SessionExample example = new SessionExample(Clock.systemDefaultZone());
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
