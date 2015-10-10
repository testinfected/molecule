package examples.ssl;

import com.vtence.molecule.WebServer;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

/**
 * <p>
 *     In this example we show how to create an start an HTTPS server. We use a JKS keystore that contains our
 *     self-signed certificate.
 * </p>
 * <p>
 *     To generate the self-signed certificate using an 2048 bits RSA key pair, use the following command:
 *     <br>
 *     <code>keytool -genkey -keyalg RSA -alias <i>key alias</i> -keystore <i>keystore file</i>
 *     -storepass <i>store password</i> -keysize 2048</code>
 * </p>
 */
public class SSLExample {

    public void run(WebServer server) throws IOException, GeneralSecurityException {
        // That's our JKS keystore containing our certificate
        File keyStore = locateOnClasspath("ssl/keystore");
        // The password to open the keystore
        String keyStorePassword = "password";
        // The password to use the key
        String keyPassword = "password";

        // We enable TLS with our key store password and key password
        server.enableSSL(keyStore, keyStorePassword, keyPassword)
              // We a render a simple text to let our user know she is on a secure channel
              .start((request, response) -> response.done("You are on a secure channel"));
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        SSLExample example = new SSLExample();
        // Run the default web server on port 8443
        WebServer webServer = WebServer.create(8443);
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
