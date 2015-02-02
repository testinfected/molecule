package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import org.junit.Test;

import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class ServerHeaderTest {

    String serverName = "server/version";
    ServerHeader serverHeader = new ServerHeader(serverName);

    MockRequest request = new MockRequest();
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