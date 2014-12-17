package com.vtence.molecule;

import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WebServerTest {

    WebServer server;
    HttpRequest request = new HttpRequest(8080);
    HttpResponse response;

    @Test
    public void runsServerOnPort8080ByDefault() throws IOException, GeneralSecurityException {
        server = WebServer.create();
        server.start(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                response.body("It works!");
            }
        });

        response = send(request);
        response.assertContentEqualTo("It works!");
    }

    @Test
    public void knowsServerUri() throws IOException {
        server = WebServer.create("0.0.0.0", 9000);
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        assertThat("server uri", server.uri(), equalTo(URI.create("http://0.0.0.0:9000")));
    }

    @Test
    public void tellsWhenServerIsUsingSSL() throws Exception {
        server = WebServer.create("0.0.0.0", 8443).enableSSL(locateOnClasspath("ssl/keystore"), "password", "password");
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        assertThat("server uri", server.uri(), equalTo(URI.create("https://0.0.0.0:8443")));
    }

    private HttpResponse send(HttpRequest request) throws IOException {
        try {
            return request.send();
        } finally {
            server.stop();
        }
    }
}