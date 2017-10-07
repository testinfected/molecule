package examples.session;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.routing.Routes;
import com.vtence.molecule.session.CookieSessionStore;
import com.vtence.molecule.session.Session;

import java.io.IOException;
import java.time.Clock;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Here's an example of setting up HTTP sessions. We use a a signed cookie store but you could very well
 * swap the pool implementation for, e.g. an in memory session pool.
 * <p>
 * <p>
 * Each client is given a secure session identifier, stored as session cookie.
 * If the client request it, we make the cookie persistent (for 5 min).
 * Every time the session is accessed, we refresh the client cookie expiry date.
 * </p>
 * <p>
 * The session pool expires stale sessions after 30 minutes.
 * As a protection mechanism, we also expire sessions that are older than 2 days,
 * even if they have been maintained active.
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
        // Create a cookie based session store - session content is stored on the client
        // Our secret key for encrypting sessions will be "secret"
        CookieSessionStore sessions = CookieSessionStore.secure("secret");
        // Alternatively, you could use an in-memory session pool like this:
        // SessionPool sessions = SessionPool.secure();
        // Invalidate stale sessions after 30 minutes
        sessions.idleTimeout(THIRTY_MINUTES);
        // Invalidate sessions that are over 2 days old, even if they are maintained active
        sessions.timeToLive(TWO_DAYS);
        // Use the provided clock to get time
        sessions.usingClock(clock);

        // Enable cookie support
        server.add(new Cookies())
              // Track sessions using transient - a.k.a session - cookies by default
              // You can change of the name of the cookie used to track sessions
              .add(new CookieSessionTracker(sessions).usingCookieName("molecule.session"))
              .route(new Routes() {{
                  // The default route greets the signed in user
                  map("/").to(request -> {
                      // There's always a session bound to the request, although it may be empty and fresh
                      // We can safely read a new session. The session won't be saved to the pool unless
                      // it's been written or it was already present in the pool.
                      Session session = Session.get(request);
                      // If our user has already identified to our site,
                      // we have a username stored in the session
                      String username = session.contains("username") ? session.<String>get("username") : "Guest";
                      return Response.ok()
                                     .done("Hello, " + username);
                  });

                  // The sign in route
                  post("/login").to(request -> {
                      // We expect a username parameter
                      String username = request.parameter("username");
                      Session session = Session.get(request);
                      // Store the username in the session. Since the session has been written to,
                      // it will automatically be saved to the pool by the end of the request cycle
                      session.put("username", username);

                      // If remember me is checked, make session cookie persistent with a max age of 5 minutes
                      if (request.hasParameter("remember_me")) {
                          session.maxAge(FIVE_MINUTES);
                      }

                      // If renew, make a fresh session to avoid session fixation attacks
                      // by generating a new session id
                      if (request.hasParameter("renew")) {
                          Session freshSession = new Session();
                          freshSession.merge(session);
                          freshSession.bind(request);
                      }

                      return Response.redirect("/")
                                     .done();
                  });

                 // The sign out route
                  delete("/logout").to(request -> {
                      Session session = Session.get(request);
                      // We invalidate the session, which prevents further use and removes the session
                      // from the pool at the end of the request cycle
                      session.invalidate();
                      return Response.redirect("/")
                                     .done();
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
