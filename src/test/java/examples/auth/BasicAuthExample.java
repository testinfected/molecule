package examples.auth;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.http.MimeTypes;
import com.vtence.molecule.middlewares.BasicAuthentication;

import java.io.IOException;

/**
 * <p>
 * In this example we demonstrate HTTP Basic Authentication, as per RFC 2617.
 * </p>
 * <p>
 * We use the {@link BasicAuthentication} middleware with a custom {@link Authenticator} to protect an application.
 * It checks if a username and password pair are valid and issues a challenge if not.
 * </p>
 * <p>
 * If the credentials are valid, the username is made available as a request attribute.
 * </p>
 */
public class BasicAuthExample {
    private final String realm;

    public BasicAuthExample(String realm) {
        this.realm = realm;
    }

    public void run(WebServer server) throws IOException {
        // Use HTTP Basic Authentication to protect our application
        server.add(new BasicAuthentication(realm))
              .start((request, response) -> {
                  // Authenticated username is available as the REMOTE_USER request attribute
                  String username = request.attribute("REMOTE_USER");
                  response.contentType(MimeTypes.TEXT).done("Hello, " + username);
              });
    }

    public static void main(String[] args) throws IOException {
        BasicAuthExample example = new BasicAuthExample("WallyWorld");
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}

