package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.Dates;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.http.HttpDate.format;
import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.support.Dates.aDate;
import static com.vtence.molecule.support.Dates.instant;
import static com.vtence.molecule.support.Dates.now;
import static org.hamcrest.Matchers.nullValue;

public class ConditionalGetTest {

    long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    ConditionalGet conditional = new ConditionalGet();

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Test public void
    sendsNotModifiedWithoutMessageBodyWhenETagIndicatesEntityIsCurrent() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678")
                        .contentType("text/plain").contentLength(32).body("response content");
            }
        });

        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);

        response.assertStatus(NOT_MODIFIED);
        response.assertContentSize(0);
        response.assertContentType(nullValue());
        response.assertHeader("Content-Length", nullValue());

    }

    @Test public void
    leavesResponseUnchangedWhenCacheValidatorsAreMissing() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("response content");
            }
        });

        conditional.handle(request, response);

        response.assertStatus(OK);
        response.assertBody("response content");
    }

    @Test public void
    ignoresCacheValidatorsIfResponseNotOK() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(CREATED).set("ETag", "12345678");
            }
        });

        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);

        response.assertStatus(CREATED);
    }

    @Test public void
    supportsHeadRequests() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678");
            }
        });

        request.method("HEAD").header("If-None-Match", "12345678");
        conditional.handle(request, response);

        response.assertStatus(NOT_MODIFIED);
    }

    @Test public void
    ignoresNonGetOrHeadRequests() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678");
            }
        });

        request.method("POST").header("If-None-Match", "12345678");
        conditional.handle(request, response);

        response.assertStatus(OK);
    }

    @Test public void
    sendsNotModifiedWhenEntityHasNotBeenModifiedSinceLastServed() throws Exception {
        final String lastModification = format(now().toDate());
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("Last-Modified", lastModification);
            }
        });

        request.header("If-Modified-Since", lastModification);
        conditional.handle(request, response);

        response.assertStatus(NOT_MODIFIED);
    }

    @Test public void
    leavesResponseUnchangedWhenEntityHasNotBeenModifiedButETagIndicatesItIsNotCurrent() throws Exception {
        final String lastModification = format(aDate().toDate());
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678").set("Last-Modified", lastModification);
            }
        });

        request.header("If-None-Match", "87654321")
               .header("If-Modified-Since", lastModification);
        conditional.handle(request, response);

        response.assertStatus(OK);
    }

    @Test public void
    leavesResponseUnchangedWhenEntityWasModifiedButETagIndicatesItIsCurrent() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.set("ETag", "12345678")
                        .set("Last-Modified", format(now().toDate()));
            }
        });

        request.header("If-None-Match", "12345678")
               .header("If-Modified-Since", format(oneHourAgo().toDate()));
        conditional.handle(request, response);

        response.assertStatus(OK);
    }

    private Dates oneHourAgo() {
        return instant(System.currentTimeMillis() - ONE_HOUR);
    }
}
