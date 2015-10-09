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

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

public class SessionExample {

    private static final int FIVE_MINUTES = (int) MINUTES.toSeconds(5);
    private static final int THIRTY_MINUTES = (int) MINUTES.toSeconds(30);
    private static final int TWO_DAYS = (int) DAYS.toSeconds(2);

    private final Clock clock;

    public SessionExample(Clock clock) {
        this.clock = clock;
    }

    public void run(WebServer server) throws IOException {
        // Create an in-memory session pool which invalidates stale sessions after 30 minutes
        SessionPool sessionPool = new SessionPool(new SecureIdentifierPolicy(), clock).idleTimeout(THIRTY_MINUTES);
        // Invalidate sessions that are over 2 days old, even if they are maintained active
        sessionPool.timeToLive(TWO_DAYS);

              // Enable cookie support
        server.add(new Cookies())
              // Track sessions using transient - a.k.a session - cookies by default
              // You decide of the cookie name to track sessions
              .add(new CookieSessionTracker(sessionPool).usingCookieName("molecule.session"))
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

                             // If remember me, make session cookie persistent with a max age of 5 minutes
                             boolean rememberMe = request.parameter("remember_me") != null;
                             if (rememberMe) {
                                 session.maxAge(FIVE_MINUTES);
                             }

                             // If renew, make a fresh session to avoid session fixation attack
                             // by generating a new session id
                             boolean renew = request.parameter("renew") != null;
                             if (renew) {
                                 Session freshSession = new Session();
                                 freshSession.merge(session);
                                 freshSession.bind(request);
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
