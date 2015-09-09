package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ETagTest {

    ETag etag = new ETag();

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsETagByComputingMD5HashOfResponseBody() throws Exception {
        etag.handle(request, response);
        response.body("response body").done();

        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotSetETagIfBodyIsEmpty() throws Exception {
        etag.handle(request, response);
        response.body("").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfStatusIsNotCacheable() throws Exception {
        etag.handle(request, response);
        response.status(NOT_FOUND).body("Not found: resource").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willSetETagIfStatusIsCreated() throws Exception {
        etag.handle(request, response);
        response.status(CREATED).body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotOverwriteETagIfAlreadySet() throws Exception {
        etag.handle(request, response);
        response.header("ETag", "already set").body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("ETag", "already set");
    }

    @Test public void
    willNotSetETagIfLastModifiedHeaderSet() throws Exception {
        etag.handle(request, response);
        response.header("Last-Modified", httpDate(new Date())).body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfCacheControlSetToNoCache() throws Exception {
        etag.handle(request, response);
        response.header("Cache-Control", "private; no-cache").body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    setsCacheControlDirectiveToNoCachingIfNoneSet() throws Exception {
        etag.handle(request, response);
        response.body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    willNoOverwriteCacheControlDirectiveIfAlreadySet() throws Exception {
        etag.handle(request, response);
        response.header("Cache-Control", "public").body("response body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Cache-Control", "public");
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}