package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static org.hamcrest.Matchers.nullValue;

public class ConditionalGetTest {

    ConditionalGet get = new ConditionalGet();

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Test public void
    sendsNotModifiedAndTruncateBodyWhenIfNoneMatchMatchesETag() throws Exception {
        get.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678");
                response.contentType("text/plain");
                response.contentLength(42);
                response.body("response content");
            }
        });

        request.header("If-None-Match", "12345678");
        get.handle(request, response);

        response.assertStatus(NOT_MODIFIED);
        response.assertContentSize(0);
        response.assertContentType(nullValue());
        response.assertHeader("Content-Length", nullValue());

    }

    @Test public void
    leavesResponsesWithoutETagUnchanged() throws Exception {
        get.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("response content");
            }
        });

        request.header("If-None-Match", "12345678");
        get.handle(request, response);

        response.assertStatus(OK);
        response.assertBody("response content");

    }

    @Test public void
    doesNotAffectResponsesThatAreNotOK() throws Exception {
        get.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678");
                response.body("resource representation");
                response.status(CREATED);
            }
        });

        request.header("If-None-Match", "12345678");
        get.handle(request, response);

        response.assertStatus(CREATED);
        response.assertBody("resource representation");
    }

    @Test public void
    ignoresNonGetRequests() throws Exception {
        get.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678");
                response.body("response content");
            }
        });

        request.method("POST").header("If-None-Match", "12345678");
        get.handle(request, response);

        response.assertStatus(OK);
        response.assertBody("response content");
    }
}
