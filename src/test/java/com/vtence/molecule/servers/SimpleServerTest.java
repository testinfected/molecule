package com.vtence.molecule.servers;

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
        server.run((request, response) -> response.body("<html>...</html>").done());

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .isChunked();
    }
}
