package examples.ssl;

import com.vtence.molecule.WebServer;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

public class SSLExample {

    public void run(WebServer server) throws IOException, GeneralSecurityException {
        // To generate a self-signed certificate using an 2048 bits RSA key pair, use the following command:
        // keytool -genkey -keyalg RSA -alias <key alias> -keystore <keystore file> -storepass <store password> -keysize 2048
        server.enableSSL(locateOnClasspath("ssl/keystore"), "password", "password")
              .start((request, response) -> {
                  response.body("You are on a secure channel");
              });
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        SSLExample example = new SSLExample();
        // Run the default web server on port 8443
        WebServer webServer = WebServer.create(8443);
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}