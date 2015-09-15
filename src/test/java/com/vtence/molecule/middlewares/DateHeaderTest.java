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
    Instant currentTime = LocalDateTime.of(2012, 6, 8, 0, 0, 0).toInstant(ZoneOffset.of("-04:00"));
    DateHeader dateHeader = new DateHeader(Clock.fixed(currentTime, ZoneId.systemDefault()));

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsDateHeaderFromClockTimeOnceDoneIfMissing() throws Exception {
        dateHeader.handle(request, response);
        assertThat(response).hasNoHeader("Date");

        response.done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Date", "Fri, 8 Jun 2012 04:00:00 GMT");
    }

    @Test public void
    wontOverrideExistingDateHeader() throws Exception {
        dateHeader.handle(request, response);
        response.header("Date", "now").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Date", "now");
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}