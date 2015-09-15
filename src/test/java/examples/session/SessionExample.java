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

    private final Clock clock;

    private CookieSessionTracker sessionTracker;

    public SessionExample(Clock clock) {
        this.clock = clock;
    }

    public void expireAfter(int seconds) {
        this.sessionTracker.expireAfter(seconds);
    }

    public void run(WebServer server) throws IOException {
        // Track sessions using a cookie strategy and an in-memory session pool
        sessionTracker = new CookieSessionTracker(new SessionPool(new SecureIdentifierPolicy(), clock));
        // Enable cookie support
        server.add(new Cookies())
              .add(sessionTracker)
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