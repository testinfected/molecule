package com.vtence.molecule;

import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WebServerTest {

    WebServer server;
    HttpRequest request = new HttpRequest(8080);
    HttpResponse response;

    @After
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void runsServerOnPort8080ByDefault() throws IOException, GeneralSecurityException {
        server = WebServer.create();
        server.start(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                response.body("It works!");
            }
        });

        response = request.get("/");
        assertThat(response).hasBodyText("It works!");
    }

    @Test
    public void knowsServerUri() throws IOException {
        server = WebServer.create("0.0.0.0", 9000);
        assertThat("server uri", server.uri(), equalTo(URI.create("http://0.0.0.0:9000")));
    }

    @Test
    public void tellsWhenServerIsUsingSSL() throws Exception {
        server = WebServer.create("0.0.0.0", 8443).enableSSL(locateOnClasspath("ssl/keystore"), "password", "password");
        assertThat("server uri", server.uri(), equalTo(URI.create("https://0.0.0.0:8443")));
    }
}