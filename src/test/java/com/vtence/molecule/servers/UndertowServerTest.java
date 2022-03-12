package com.vtence.molecule.servers;

import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import org.junit.Test;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class UndertowServerTest extends ServerCompatibilityTests {

    protected Server createServer(String host, int port) {
        return new UndertowServer(host, port);
    }

    @Test public void
    setsContentLengthAutomaticallyForSmallResponses() throws Exception {
        server.start(request -> Response.ok().done("<html>...</html>"));

        var response = client.send(request.uri(server.uri()).build(), ofString());
        assertNoError();
        assertThat(response).hasBody("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }

    @Test public void
    supportsHttp2() throws Exception {
        server.enableHTTP2().start(request -> Response.ok().done(request.protocol()));

        var response = client.send(request.uri(server.uri().resolve("/")).build(), ofString());
        assertNoError();
        assertThat(response).hasBody("HTTP/2.0");
    }
}
