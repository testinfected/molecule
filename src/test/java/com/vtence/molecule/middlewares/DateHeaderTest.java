package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.support.BrokenClock;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static com.vtence.molecule.support.DateBuilder.calendarDate;
import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class DateHeaderTest {
    Mockery context = new JUnit4Mockery();

    Date now = calendarDate(2012, 6, 8).atMidnight().inZone("GMT-04:00").build();
    DateHeader dateHeader = new DateHeader(BrokenClock.stoppedAt(now));
    Application successor = context.mock(Application.class, "successor");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor()  {
        dateHeader.connectTo(successor);
    }

    @Test public void
    setsDateHeaderFromClockTime() throws Exception {
        context.checking(new Expectations() {{
            oneOf(successor).handle(with(request), with(response));
        }});

        dateHeader.handle(request, response);
        response.assertHeader("Date", "Fri, 08 Jun 2012 04:00:00 GMT");
    }
}