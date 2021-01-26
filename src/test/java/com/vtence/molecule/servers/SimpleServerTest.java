package com.vtence.molecule.servers;

import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import org.junit.Test;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleServerTest extends ServerCompatibilityTests {

    protected Server createServer(String host, int port) {
        return new SimpleServer(host, port);
    }

    @Test public void
    chunksResponseWhenContentLengthUnknown() throws Exception {
        server.start(request -> Response.ok().done("<html>...</html>"));

        var response = client.send(request.version(HTTP_1_1).uri(server.uri()).build(), ofString());
        assertNoError();
        assertThat(response).hasBody("<html>...</html>")
                            .isChunked();
    }
}
