package examples.ssl;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.Trust;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.ssl.SecureProtocol.TLS;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SSLTest {

    SSLExample ssl = new SSLExample();

    WebServer secure = WebServer.create("localhost", 9443);
    WebServer insecure = WebServer.create("localhost", 9999);

    HttpClient client = HttpClient.newBuilder().sslContext(setupSSL()).build();
    HttpRequest.Builder request = HttpRequest.newBuilder();


    @Before
    public void startServer() throws IOException, GeneralSecurityException {
        ssl.redirect(insecure, secure);
        ssl.run(secure);
    }

    @After
    public void stopServer() throws IOException {
        secure.stop();
        insecure.stop();
    }

    @Test
    public void connectingSecurely() throws Exception {
        var response = client.send(request.uri(secure.uri()).GET().build(),
                                   ofString());

        assertThat(response).hasBody("You are on a secure channel")
                            .hasHeader("Strict-Transport-Security", "max-age=31536000");
    }

    @Test
    public void redirectingToASecureConnection() throws Exception {
        var response = client.send(request.uri(insecure.uri().resolve("/resource"))
                                          .GET().build(),
                                   ofString());

        assertThat(response).hasStatusCode(301)
                            .hasHeader("Location", "https://localhost:9443/resource");
    }

    private static SSLContext setupSSL() {
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

        SSLContext context;
        try {
            context = TLS.initialize(null, new TrustManager[]{Trust.allCertificates()});
        } catch (GeneralSecurityException e) {
            throw new AssertionError("Failed to setup  SSL", e);
        }
        return context;
    }
}