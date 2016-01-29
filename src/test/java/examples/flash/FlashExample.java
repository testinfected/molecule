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
                      FlashHash flash = FlashHash.get(request);

                      String email = request.parameter("email");
                        if (email != null) {
                          flash.notice("Account '" + email + "' successfully created");
                      } else {
                          flash.alert("An email is required");
                      }
                      response.redirectTo("/account").done();
                  });

                  get("/account").to((request, response) -> {
                      FlashHash flash = FlashHash.get(request);

                      if (flash.notice() != null) {
                          response.done(flash.notice());
                      } else if (flash.alert() != null) {
                          response.done(flash.alert());
                      } else {
                          response.done();
                      }
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
