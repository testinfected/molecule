package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ConditionalGetTest {

    ConditionalGet conditional = new ConditionalGet();

    @Test
    public void
    sendsNotModifiedWithoutMessageBodyWhenGettingEntityWhoseRepresentationHasNotChanged() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .header("ETag", "12345678")
                                                                .contentType("text/plain").contentLength(32)
                                                                .done("response content"))
                                       .handle(Request.get("/")
                                                      .header("If-None-Match", "12345678"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(NOT_MODIFIED)
                            .hasBodySize(0)
                            .hasNoHeader("Content-Type")
                            .hasNoHeader("Content-Length");
    }

    @Test
    public void
    leavesResponseUnchangedOnGetWhenCacheValidatorsAreMissing() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .done("response content"))
                                       .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(OK)
                            .hasBodyText("response content");
    }

    @Test
    public void
    ignoresCacheValidatorsOnGetIfResponseNotOK() throws Exception {
        Response response = conditional.then(request -> Response.of(CREATED)
                                                                .header("ETag", "12345678")
                                                                .done())
                                       .handle(Request.get("/")
                                                      .header("If-None-Match", "12345678"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(CREATED);
    }

    @Test
    public void
    appliesConditionalLogicToHeadRequestsAsWell() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .header("ETag", "12345678")
                                                                .done())
                                       .handle(Request.head("/")
                                                      .header("If-None-Match", "12345678"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    ignoresNonGetOrHeadRequests() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .header("ETag", "12345678")
                                                                .done())
                                       .handle(Request.post("/")
                                                      .header("If-None-Match", "12345678"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    sendsNotModifiedWhenGettingEntityWhichHasNotBeenModifiedSinceLastServed() throws Exception {
        final String lastModification = httpDate(Instant.now());

        Response response = conditional.then(request -> Response.ok()
                                                                .header("Last-Modified", lastModification)
                                                                .done())
                                       .handle(Request.get("/")
                                                      .header("If-Modified-Since", lastModification));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityHasNotBeenModifiedButETagIndicatesItIsNotCurrent() throws Exception {
        final String lastModification = httpDate(Instant.now());

        Response response = conditional.then(request -> Response.ok()
                                                                .header("ETag", "12345678")
                                                                .header("Last-Modified", lastModification)
                                                                .done())
                                       .handle(Request.get("/")
                                                      .header("If-None-Match", "87654321")
                                                      .header("If-Modified-Since", lastModification));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityWasModifiedButETagIndicatesItIsCurrent() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .header("ETag", "12345678")
                                                                .header("Last-Modified", httpDate(Instant.now()))
                                                                .done())
                                       .handle(Request.get("/")
                                                      .header("If-None-Match", "12345678")
                                                      .header("If-Modified-Since", httpDate(oneHourAgo())));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    leavesResponseUnchangedIfModifiedSinceDateFormatIsNotSupported() throws Exception {
        Response response = conditional.then(request -> Response.ok()
                                                                .header("Last-Modified", httpDate(Instant.now()))
                                                                .done())
                                       .handle(Request.get("/")
                                                      .header("If-Modified-Since", "???"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(OK);
    }

    private Instant oneHourAgo() {
        return Instant.now().minus(1, ChronoUnit.HOURS);
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}