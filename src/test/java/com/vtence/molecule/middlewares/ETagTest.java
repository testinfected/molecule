package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.support.MockRequest;
import org.junit.Test;

import java.util.Date;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class ETagTest {

    ETag etag = new ETag();

    MockRequest request = new MockRequest();
    Response response = new Response();

    @Test public void
    setsETagByComputingMD5HashOfResponseBody() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotSetETagIfBodyIsEmpty() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfStatusIsNotCacheable() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(HttpStatus.NOT_FOUND);
                response.body("Not found: resource");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willSetETagIfStatusIsCreated() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(HttpStatus.CREATED);
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasHeader("ETag", "\"91090ad25c02ffd89cd46ae8b28fcdde\"");
    }

    @Test public void
    willNotOverwriteETagIfAlreadySet() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "already set");
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasHeader("ETag", "already set");
    }

    @Test public void
    willNotSetETagIfLastModifiedHeaderSet() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Last-Modified", httpDate(new Date()));
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    willNotSetETagIfCacheControlSetToNoCache() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Cache-Control", "private; no-cache");
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasNoHeader("ETag");
    }

    @Test public void
    setsCacheControlDirectiveToNoCachingIfNoneSet() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    willNoOverwriteCacheControlDirectiveIfAlreadySet() throws Exception {
        etag.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Cache-Control", "public");
                response.body("response body");
            }
        });
        etag.handle(request, response);
        assertThat(response).hasHeader("Cache-Control", "public");
    }
}