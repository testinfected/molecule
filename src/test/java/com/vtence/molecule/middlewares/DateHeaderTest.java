package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class DateHeaderTest {
    Instant currentTime = LocalDateTime.of(2012, 6, 8, 0, 0, 0)
                                       .toInstant(ZoneOffset.of("-04:00"));
    DateHeader dateHeader = new DateHeader(Clock.fixed(currentTime, ZoneId.systemDefault()));

    @Test public void
    setsDateHeaderFromClockTimeOnceDoneIfMissing() throws Exception {
        Response response = dateHeader.then(request -> Response.ok())
                                      .handle(Request.get("/"));

        assertThat(response).hasNoHeader("Date");

        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Date", "Fri, 8 Jun 2012 04:00:00 GMT");
    }

    @Test public void
    wontOverrideExistingDateHeader() throws Exception {
        Response response = dateHeader.then(request -> Response.ok()
                                                               .header("Date", "now")
                                                               .done())
                                      .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Date", "now");
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}