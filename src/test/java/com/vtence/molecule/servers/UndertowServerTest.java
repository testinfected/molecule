package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.LogManager;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class UndertowServerTest extends ServerCompatibilityTests {

    @BeforeClass public static void
    silenceLogging() {
        LogManager.getLogManager().reset();
    }

    protected Server createServer(String host, int port) {
        return new UndertowServer(host, port);
    }

    @Test public void
    setsContentLengthAutomaticallyForSmallResponses() throws IOException {
        server.run(Application.of(request -> Response.ok().done("<html>...</html>")));

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }
}
