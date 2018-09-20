package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.TextBody;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ETagTest {

    ETag etag = new ETag();

    @Test public void
    setsETagByComputingMD5HashOfResponseBody() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotSetETagIfBodyIsEmpty() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .done(""))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfStatusIsNotCacheable() throws Exception {
        Response response = etag.then(request -> Response.of(NOT_FOUND)
                                                         .done("Not found: resource"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willSetETagIfStatusIsCreated() throws Exception {
        Response response = etag.then(request -> Response.of(CREATED)
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotOverwriteETagIfAlreadySet() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .header("ETag", "already set")
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("ETag", "already set");
    }

    @Test public void
    willNotSetETagIfLastModifiedHeaderSet() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .header("Last-Modified", Instant.now())
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfCacheControlSetToNoCache() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .header("Cache-Control", "private; no-cache")
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    setsCacheControlDirectiveToNoCachingIfNoneSet() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    willNoOverwriteCacheControlDirectiveIfAlreadySet() throws Exception {
        Response response = etag.then(request -> Response.ok()
                                                         .header("Cache-Control", "public")
                                                         .done("response body"))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Cache-Control", "public");
    }

    @Test public void
    closesOriginalBodyAfterComputingETag() throws Exception {
        CloseableBody originalBody = new CloseableBody();
        Response response = etag.then(request -> Response.ok()
                                                         .done(originalBody))
                                .handle(Request.get("/"));

        assertNoExecutionError(response);

        assertThat("closed?", originalBody.closed, is(true));
    }

    public static class CloseableBody extends TextBody {
        public boolean closed;

        public CloseableBody() {
            append("Close me!");
        }

        public void close() {
            closed = true;
        }
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}