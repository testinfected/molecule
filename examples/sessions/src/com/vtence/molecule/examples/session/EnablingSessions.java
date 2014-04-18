package com.vtence.molecule.examples.session;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.MiddlewareStack;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.session.SessionPool;

import java.io.IOException;

import static com.vtence.molecule.middlewares.Router.draw;

public class EnablingSessions {

    public static void main(String[] args) throws IOException {
        SimpleServer server = new SimpleServer(8080);

        server.run(new MiddlewareStack() {{
            // Track sessions using a cookie strategy and an in-memory session pool and make
            // sessions expire after 5 minutes (i.e. 300s)
            use(new CookieSessionTracker(new SessionPool()).expireAfter(300));

            run(draw(new DynamicRoutes() {{
                map("/").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        Session session = request.attribute(Session.class);
                        String username = session.contains("username") ? session.<String>get("username") : "Guest";
                        response.body("Welcome, " + username);
                    }
                });

                post("/login").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        String username = request.parameter("username");
                        Session session = request.attribute(Session.class);
                        session.put("username", username);
                        response.redirectTo("/");
                    }
                });

                delete("/logout").to(new Application() {
                    public void handle(Request request, Response response) throws Exception {
                        Session session = request.attribute(Session.class);
                        session.invalidate();
                        response.redirectTo("/");
                    }
                });
            }}));
        }});
    }
}
