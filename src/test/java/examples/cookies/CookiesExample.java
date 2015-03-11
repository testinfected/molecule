package examples.cookies;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.middlewares.Cookies;

import java.io.IOException;

public class CookiesExample {

    public void run(WebServer server) throws IOException {
        server.add(new Cookies())
              .start(new Application() {
                  public void handle(Request request, Response response) throws Exception {
                      CookieJar cookies = CookieJar.get(request);
                      Cookie profile = cookies.get("profile");
                      Cookie location = cookies.get("location");
                      response.body(String.format("profile: %s, location: %s", profile.value(), location.value()));
                  }
              });
    }

    public static void main(String[] args) throws IOException {
        CookiesExample example = new CookiesExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}