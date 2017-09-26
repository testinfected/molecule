package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class SimpleServerTest extends ServerCompatibilityTests {

    protected Server createServer(String host, int port) {
        return new SimpleServer(host, port);
    }

    @Test public void
    chunksResponseWhenContentLengthUnknown() throws IOException {
        server.run(Application.of(request -> Response.ok().done("<html>...</html>")));

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .isChunked();
    }
}
