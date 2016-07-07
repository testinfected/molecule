package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class BasicAuthenticationTest {

    BasicAuthentication authentication = new BasicAuthentication("WallyWorld");

    Request request = new Request();
    Response response = new Response();

    @Test
    public void
    issuesAChallengeWhenNoCredentialsAreSpecified() throws Exception {
        authentication.handle(request, response);

        assertThat(response).hasStatus(UNAUTHORIZED)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .hasContentType("text/plain")
                            .isEmpty()
                            .isDone();
    }
}