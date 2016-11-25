package examples.ssl;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.ForceSSL;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

/**
 * <p>
 *     In this example we create and start an HTTPS server. We use a JKS keystore that contains our
 *     self-signed certificate. Alongside the secure server we start a insecure HTTP server, which redirects
 *     to the secure server.
 * </p>
 * <p>
 *     To generate the self-signed certificate using an 2048 bits RSA key pair, use the following command:
 *     <br>
 *     <code>keytool -genkey -keyalg RSA -alias <i>key alias</i> -keystore <i>keystore file</i>
 *     -storepass <i>store password</i> -keysize 2048</code>
 * </p>
 */
public class SSLExample {

    public void redirect(WebServer insecure, WebServer secure) throws IOException {
        // Redirect users to the secure connection
        insecure.start(new ForceSSL().redirectTo(secure.uri().getAuthority()));
    }

    public void run(WebServer server) throws IOException, GeneralSecurityException {
        // That's our JKS keystore containing our certificate
        File keyStore = locateOnClasspath("ssl/keystore");
        // The password to open the keystore
        String keyStorePassword = "password";
        // The password to use the key
        String keyPassword = "password";

        // We enable TLS with our key store password and key password
        server.enableSSL(keyStore, keyStorePassword, keyPassword)
              // Add HSTS security headers
              .add(new ForceSSL())
              // We a render a simple text to let our user know she is on a secure channel
              .start((request, response) -> response.done("You are on a secure channel"));
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        SSLExample example = new SSLExample();
        // Run the insecure web server on port 8080
        WebServer insecure = WebServer.create(8080);
        // Run the secure (SSL) web server on port 8443
        WebServer secure = WebServer.create(8443);
        example.redirect(insecure, secure);
        example.run(secure);
        System.out.println("Access at " + insecure.uri());
    }
}
