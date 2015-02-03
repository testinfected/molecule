package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.Dates;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.support.Dates.aDate;
import static com.vtence.molecule.support.Dates.instant;
import static com.vtence.molecule.support.Dates.now;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class ConditionalGetTest {

    long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    ConditionalGet conditional = new ConditionalGet();

    Request request = new Request().method(GET);
    Response response = new Response();

    @Test
    public void
    sendsNotModifiedWithoutMessageBodyWhenGettingEntityWhoseRepresentationHasNotChanged() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "12345678")
                        .contentType("text/plain").contentLength(32).body("response content");
            }
        });

        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);

        assertThat(response).hasStatus(NOT_MODIFIED)
                .hasBodySize(0)
                .hasNoHeader("Content-Type")
                .hasNoHeader("Content-Length");
    }

    @Test
    public void
    leavesResponseUnchangedOnGetWhenCacheValidatorsAreMissing() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("response content");
            }
        });

        conditional.handle(request, response);

        assertThat(response).hasStatus(OK)
                .hasBodyText("response content");
    }

    @Test
    public void
    ignoresCacheValidatorsOnGetIfResponseNotOK() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(CREATED).header("ETag", "12345678");
            }
        });

        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);

        assertThat(response).hasStatus(CREATED);
    }

    @Test
    public void
    appliesConditionalLogicToHeadRequestsAsWell() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "12345678");
            }
        });

        request.method(HEAD).header("If-None-Match", "12345678");
        conditional.handle(request, response);

        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    ignoresNonGetOrHeadRequests() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "12345678");
            }
        });

        request.method(POST).header("If-None-Match", "12345678");
        conditional.handle(request, response);

        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    sendsNotModifiedWhenGettingEntityWhichHasNotBeenModifiedSinceLastServed() throws Exception {
        final String lastModification = httpDate(now().toDate());
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Last-Modified", lastModification);
            }
        });

        request.header("If-Modified-Since", lastModification);
        conditional.handle(request, response);

        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityHasNotBeenModifiedButETagIndicatesItIsNotCurrent() throws Exception {
        final String lastModification = httpDate(aDate().toDate());
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "12345678").header("Last-Modified", lastModification);
            }
        });

        request.header("If-None-Match", "87654321")
                .header("If-Modified-Since", lastModification);
        conditional.handle(request, response);

        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityWasModifiedButETagIndicatesItIsCurrent() throws Exception {
        conditional.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("ETag", "12345678")
                        .header("Last-Modified", httpDate(now().toDate()));
            }
        });

        request.header("If-None-Match", "12345678")
                .header("If-Modified-Since", httpDate(oneHourAgo().toDate()));
        conditional.handle(request, response);

        assertThat(response).hasStatus(OK);
    }

    private Dates oneHourAgo() {
        return instant(System.currentTimeMillis() - ONE_HOUR);
    }
}