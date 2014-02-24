package com.vtence.molecule.examples.session;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.simple.session.CookieTracker;
import com.vtence.molecule.simple.session.SessionPool;

import java.io.IOException;

import static com.vtence.molecule.middlewares.Router.draw;

public class EnablingSessions {

    public static void main(String[] args) throws IOException {
        SimpleServer server = new SimpleServer(8080);

        // Enable sessions, using a cookie tracking strategy with an in-memory session pool
        server.enableSessions(new CookieTracker(new SessionPool()));

        server.run(draw(new DynamicRoutes() {{

            map("/").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    Session session = request.session(false);
                    String username = (session != null) ?
                            session.<String>get("username") : "Guest";
                    response.body("Welcome, " + username);
                }
            });

            post("/login").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    String username = request.parameter("username");
                    request.session().put("username", username);
                    response.redirectTo("/");
                }
            });

            delete("/logout").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    Session session = request.session(false);
                    if (session != null) session.invalidate();
                    response.redirectTo("/");
                }
            });
        }}));
    }
}
