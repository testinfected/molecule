package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class ServerHeaderTest {

    String serverName = "server/version";
    ServerHeader serverHeader = new ServerHeader(serverName);

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsServerHeader() throws Exception {
        serverHeader.connectTo(writeToBody(serverName));
        serverHeader.handle(request, response);
        assertThat(response).hasBodyText(serverName);
    }

    private Application writeToBody(final String text) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(text);
            }
        };
    }
}