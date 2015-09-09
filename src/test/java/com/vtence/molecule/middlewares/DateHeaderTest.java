package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.BrokenClock;
import org.junit.Test;

import java.util.Date;

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
        assertThat(response).hasHeader("Date", "Fri, 08 Jun 2012 04:00:00 GMT");

        response.await();
    }

    @Test public void
    wontOverrideExistingDateHeader() throws Exception {
        dateHeader.handle(request, response);
        response.header("Date", "now").done();

        assertThat(response).hasHeader("Date", "now");
        response.await();
    }
}