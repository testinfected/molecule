package examples.auth;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.http.MimeTypes;
import com.vtence.molecule.lib.Authenticator;
import com.vtence.molecule.middlewares.BasicAuthentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;

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

    private final Map<String, String> users = new HashMap<>();

    public BasicAuthExample(String realm) {
        this.realm = realm;
    }

    public void addUser(String username, String password) {
        users.put(username, password);
    }

    public void run(WebServer server) throws IOException {
        // Use HTTP Basic Authentication to protect our application
        server.add(new BasicAuthentication(realm, this::authenticate))
              .start((request, response) -> {
                  // Authenticated username is available as the REMOTE_USER request attribute
                  String username = request.attribute("REMOTE_USER");
                  response.contentType(MimeTypes.TEXT).done("Hello, " + username);
              });
    }

    public Optional<String> authenticate(String... credentials) {
        String username = readUsernameFrom(credentials);
        String password = readPasswordFrom(credentials);
        if (!users.containsKey(username)) return Optional.empty();

        return users.get(username).equals(password) ? Optional.of(username) : Optional.empty();
    }

    private String readUsernameFrom(String... credentials) {
        return stream(credentials).findFirst().orElse(null);
    }

    private String readPasswordFrom(String... credentials) {
        return stream(credentials).skip(1).findFirst().orElse(null);
    }

    public static void main(String[] args) throws IOException {
        BasicAuthExample example = new BasicAuthExample("WallyWorld");
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}

