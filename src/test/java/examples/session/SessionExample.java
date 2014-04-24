package examples.session;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.session.SecureIdentifierPolicy;
import com.vtence.molecule.session.SessionPool;
import com.vtence.molecule.util.Clock;
import com.vtence.molecule.util.SystemClock;

import java.io.IOException;

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
        server.add(sessionTracker)
              .start(new DynamicRoutes() {{
                         map("/").to(new Application() {
                             public void handle(Request request, Response response) throws Exception {
                                 Session session = Session.get(request);
                                 String username = session.contains("username") ? session.<String>get("username") : "Guest";
                                 response.body("Hello, " + username);
                             }
                         });

                         post("/login").to(new Application() {
                             public void handle(Request request, Response response) throws Exception {
                                 String username = request.parameter("username");
                                 Session session = Session.get(request);
                                 session.put("username", username);
                                 response.redirectTo("/");
                             }
                         });

                         delete("/logout").to(new Application() {
                             public void handle(Request request, Response response) throws Exception {
                                 Session session = Session.get(request);
                                 session.invalidate();
                                 response.redirectTo("/");
                             }
                         });
                     }}
              );
    }

    public static void main(String[] args) throws IOException {
        // Run server on a random available port...
        WebServer webServer = WebServer.create();
        new SessionExample(new SystemClock()).run(webServer);
        System.out.println("Running on " + webServer.uri());
    }
}
