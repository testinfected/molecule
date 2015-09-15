package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ServerHeaderTest {

    String serverName = "server/version";
    ServerHeader serverHeader = new ServerHeader(serverName);

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsServerHeaderIfNotPresentOnceDone() throws Exception {
        serverHeader.handle(request, response);
        assertThat(response).hasNoHeader("Server");

        response.done();
        assertThat(response).hasHeader("Server", serverName);

        assertNoExecutionError();
    }

    @Test public void
    doesNotOverrideExistingServerHeader() throws Exception {
        serverHeader.handle(request, response);

        response.header("Server", "existing").done();

        assertThat(response).hasHeader("Server", "existing");
        assertNoExecutionError();
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}