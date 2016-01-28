package examples.flash;


import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.FlashHash;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.middlewares.Flash;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.session.SessionPool;

import java.io.IOException;

public class FlashExample {

    public void run(WebServer server) throws IOException {
        server.add(new Cookies())
              .add(new CookieSessionTracker(new SessionPool()))
              .add(new Flash())
              .start(new DynamicRoutes() {{
                  post("/accounts").to((request, response) -> {
                      String email = request.parameter("email");
                      FlashHash flash = FlashHash.get(request);
                      flash.notice("Account '" + email + "' successfully created");
                      response.redirectTo("/account").done();
                  });

                  get("/account").to((request, response) -> {
                      FlashHash flash = FlashHash.get(request);
                      String notice = flash.notice();
                      response.done(notice != null ? notice : "");
                  });
              }});
    }


    public static void main(String[] args) throws IOException {
        FlashExample example = new FlashExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
