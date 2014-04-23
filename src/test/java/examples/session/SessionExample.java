package examples.session;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.Session;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.MiddlewareStack;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.session.SecureIdentifierPolicy;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.session.SessionPool;
import com.vtence.molecule.util.Clock;
import com.vtence.molecule.util.SystemClock;

import java.io.IOException;
import java.net.InetAddress;

import static com.vtence.molecule.middlewares.Router.draw;

public class SessionExample {

    private final Clock clock;

    public SessionExample(Clock clock) {
        this.clock = clock;
    }

    public void run(Server server) throws IOException {
        server.run(new MiddlewareStack() {{
            // Track sessions using a cookie strategy and an in-memory session pool and make
            // sessions expire after 5 minutes (i.e. 300s)
            use(new CookieSessionTracker(new SessionPool(new SecureIdentifierPolicy(), clock)).expireAfter(300));

            run(draw(new DynamicRoutes() {{
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
            }}));
        }});
    }

    public static void main(String[] args) throws IOException {
        // By default, server will run on a random available port...
        SimpleServer server = new SimpleServer();
        new SessionExample(new SystemClock()).run(server);
        System.out.println("Running on http://" + InetAddress.getLocalHost().getHostName() + ":" + server.port());
    }
}
