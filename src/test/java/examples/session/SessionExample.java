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

/**
 * Here's an example of setting up HTTP sessions. We use an im-memory session pool but you could very well
 * swap the pool implementation for, e.g. a signed cookie store session pool.
 *
 * <p>
 *     Each client is given a secure session identifier, stored as session cookie.
 *     If the client request it, we make the cookie persistent (for 5 min).
 *     Every time the session is accessed, we refresh the client cookie expiry date.
 * </p>
 * <p>
 *     The session pool expires stale sessions after 30 minutes.
 *     As a protection mechanism, we also expire sessions that are older than 2 days,
 *     even if they have been maintained active.
 * </p>
 */
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
        SessionPool sessions = new SessionPool(new SecureIdentifierPolicy(), clock).idleTimeout(THIRTY_MINUTES);
        // Invalidate sessions that are over 2 days old, even if they are maintained active
        sessions.timeToLive(TWO_DAYS);
        // Alternatively, you could use cookie storage for sessions:
        // CookieSessionStore sessions = new CookieSessionStore(new SecureIdentifierPolicy(), new Base64Marshaler());

              // Enable cookie support
        server.add(new Cookies())
              // Track sessions using transient - a.k.a session - cookies by default
              // You can change of the name of the cookie used to track sessions
              .add(new CookieSessionTracker(sessions).usingCookieName("molecule.session"))
              .start(new DynamicRoutes() {{
                         // The default route greets the signed in user
                         map("/").to((request, response) -> {
                             // There's always a session bound to the request, although it may be empty and fresh
                             // We can safely read a new session. The session won't be saved to the pool unless
                             // it's been written or it was already present in the pool.
                             Session session = Session.get(request);
                             // If our user has already identified to our site,
                             // we have a username stored in the session
                             String username = session.contains("username") ? session.<String>get("username") : "Guest";
                             response.done("Hello, " + username);
                         });

                         // The sign in route
                         post("/login").to((request, response) -> {
                             // We expect a username parameter
                             String username = request.parameter("username");
                             Session session = Session.get(request);
                             // Store the username in the session. Since the session has been written to,
                             // it will automatically be saved to the pool by the end of the request cycle
                             session.put("username", username);

                             // If remember me is checked, make session cookie persistent with a max age of 5 minutes
                             boolean rememberMe = request.parameter("remember_me") != null;
                             if (rememberMe) {
                                 session.maxAge(FIVE_MINUTES);
                             }

                             // If renew, make a fresh session to avoid session fixation attacks
                             // by generating a new session id
                             boolean renew = request.parameter("renew") != null;
                             if (renew) {
                                 Session freshSession = new Session();
                                 freshSession.merge(session);
                                 freshSession.bind(request);
                             }

                             response.redirectTo("/").done();
                         });

                         // The sign out route
                         delete("/logout").to((request, response) -> {
                             Session session = Session.get(request);
                             // We invalidate the session, which prevents further use and removes the session
                             // from the pool at the end of the request cycle
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
