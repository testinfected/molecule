package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ServerHeaderTest {

    String serverName = "server/version";
    ServerHeader serverHeader = new ServerHeader(serverName);

    @Test public void
    setsServerHeaderIfNotPresentOnceDone() throws Exception {
        Response response = serverHeader.then(request -> Response.ok())
                                        .handle(Request.get("/"));
        assertThat(response).hasNoHeader("Server");

        response.done();
        assertThat(response).hasHeader("Server", serverName);

        assertNoExecutionError(response);
    }

    @Test public void
    keepsExistingServerHeaderIfAny() throws Exception {
        Response response = serverHeader.then(request -> Response.ok()
                                                                 .header("Server", "existing")
                                                                 .done())
                                        .handle(Request.get("/"));

        assertThat(response).hasHeader("Server", "existing");
        assertNoExecutionError(response);
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}