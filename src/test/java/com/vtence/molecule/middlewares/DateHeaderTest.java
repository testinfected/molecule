package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.BrokenClock;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.support.Dates.calendarDate;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class DateHeaderTest {
    Date now = calendarDate(2012, 6, 8).atMidnight().inZone("GMT-04:00").toDate();
    DateHeader dateHeader = new DateHeader(BrokenClock.stoppedAt(now));

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsDateHeaderFromClockTimeOnceDoneIfMissing() throws Exception {
        dateHeader.handle(request, response);
        assertThat(response).hasNoHeader("Date");

        response.done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Date", "Fri, 08 Jun 2012 04:00:00 GMT");
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