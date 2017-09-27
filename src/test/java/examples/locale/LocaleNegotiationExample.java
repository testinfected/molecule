package examples.locale;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Locales;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * <p>
 * This example demonstrates locale detection from the HTTP <code>Accept-Language</code> header.
 * </p>
 * <p>
 * We use the {@link Locales} middleware to detect the user agent locale and figure out the best
 * possible locale knowing the application supported locales.
 * </p>
 */
public class LocaleNegotiationExample {

    private final String[] supportedLanguages;

    /**
     * Constructs this example with the list of locales we support
     *
     * @param supportedLanguages the list of languages the application supports
     */
    public LocaleNegotiationExample(String... supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public void run(WebServer server) throws IOException {
        // Knowing what languages we support, figure out the best locale to use for each incoming request
        server.add(new Locales(supportedLanguages))
              .start(request -> {
                  // The best possible locale is available as a request attribute
                  Locale locale = request.attribute(Locale.class);
                  // Set the response to plain text
                  return Response.ok()
                                 .contentType("text/plain")
                                 .done(// What the client asked for
                                       "You asked for: " + request.header("accept-language") + "\n" +
                                       // All the locales we support
                                       "We support: " + Arrays.asList(supportedLanguages) + "\n" +
                                       // The application default locale
                                       "Our default is: " + Locale.getDefault().toLanguageTag() + "\n" +
                                       // The best locale for this request, depending on supported locales and requested locale
                                       "The best match is: " + locale.toLanguageTag() + "\n"
                                 );
              });
    }

    public static void main(String[] args) throws IOException {
        // We run the example with support for English, US English and French
        LocaleNegotiationExample example = new LocaleNegotiationExample("en", "en_US", "fr");
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
