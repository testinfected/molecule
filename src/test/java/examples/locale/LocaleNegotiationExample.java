package examples.locale;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Locales;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class LocaleNegotiationExample {

    private final String[] supportedLanguages;

    public LocaleNegotiationExample(String... supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public void run(WebServer server) throws IOException {
        // Knowing what languages we support, figure out the best locale to use for each incoming request
        server.add(new Locales(supportedLanguages))
              .start(new Application() {
                  public void handle(Request request, Response response) throws Exception {
                      response.contentType("text/plain");
                      response.body(
                              "You asked for: " + request.header("accept-language") + "\n" +
                              "We support: " + Arrays.asList(supportedLanguages) + "\n" +
                              "Our default is: " + Locale.getDefault() + "\n" +
                              "The best match is: " + request.attribute(Locale.class) + "\n"
                      );
                  }
              });
    }

    public static void main(String[] args) throws IOException {
        LocaleNegotiationExample example = new LocaleNegotiationExample("en", "en_US", "fr");
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}