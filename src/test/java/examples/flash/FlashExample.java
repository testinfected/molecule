package examples.flash;


import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.FlashHash;
import com.vtence.molecule.middlewares.CookieSessionTracker;
import com.vtence.molecule.middlewares.Cookies;
import com.vtence.molecule.middlewares.Flash;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.session.SessionPool;

import java.io.IOException;

/**
 * <p>
 * In this example we demonstrate how to use the {@link FlashHash}.
  * <p>
 * The flash is a special part of the session which is cleared with each request.
 * This means that values stored in the flash will only be available in the next request.
 * </p>
 * <p>
 * When a new user posts to /accounts to create an account, we add a message to the flash
 * and redirect to /account. A get to /account reads the message from the flash and displays
 * it to our user.
 * </p>
 */
public class FlashExample {

    public void run(WebServer server) throws IOException {
        server.add(new Cookies())
              // We'll use an in memory session pool in this example
              .add(new CookieSessionTracker(SessionPool.secure()))
              // We need the Flash middleware
              .add(new Flash())
              .route(new DynamicRoutes() {{
                  // a post to /accounts creates a new account if email is not already taken
                  post("/accounts").to(request -> {
                      FlashHash flash = FlashHash.get(request);

                      String email = request.parameter("email");
                      if (email != null) {
                          // Add a flash notice if the account was successfully created
                          flash.notice("Account '" + email + "' successfully created");
                      } else {
                          // Add a flash alert if creation failed
                          flash.alert("An email is required");
                      }
                      return Response.redirect("/account").done();
                  });

                  // a get /account displays the flash message
                  get("/account").to(request -> {
                      FlashHash flash = FlashHash.get(request);

                      // Display either the notice or alert to the user  ...
                      if (flash.notice() != null) {
                          return Response.ok().done(flash.notice());
                      }
                      if (flash.alert() != null) {
                          return Response.ok().done(flash.alert());
                      }
                      // ... or nothing
                      return Response.ok().done();
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
