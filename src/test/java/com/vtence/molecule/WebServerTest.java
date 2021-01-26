package com.vtence.molecule;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.logging.LogManager;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WebServerTest {

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder();

    WebServer server;

    @BeforeClass
    public static void silenceLogging() {
        LogManager.getLogManager().reset();
    }

    @After
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void byDefaultRunsServerOnPort8080() throws Exception {
        server = WebServer.create();
        server.start(request -> Response.ok().done("It works!"));

        var response = client.send(request.uri(server.uri()).GET().build(), ofString());
        assertThat(response).hasBody("It works!");
    }

    @Test
    public void knowsServerUri() {
        server = WebServer.create("0.0.0.0", 9000);
        assertThat("server uri", server.uri(), equalTo(URI.create("http://0.0.0.0:9000")));
    }

    @Test
    public void tellsWhenServerIsUsingSSL() throws Exception {
        server = WebServer.create("0.0.0.0", 8443).enableSSL(locateOnClasspath("ssl/keystore"), "password", "password");
        assertThat("server uri", server.uri(), equalTo(URI.create("https://0.0.0.0:8443")));
    }
}