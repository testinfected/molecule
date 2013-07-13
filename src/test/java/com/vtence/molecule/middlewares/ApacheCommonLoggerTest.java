package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.support.BrokenClock;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.vtence.molecule.HttpMethod.DELETE;
import static com.vtence.molecule.HttpMethod.GET;
import static com.vtence.molecule.HttpStatus.NO_CONTENT;
import static com.vtence.molecule.HttpStatus.OK;
import static com.vtence.molecule.support.DateBuilder.calendarDate;
import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static com.vtence.molecule.support.SetHeader.setHeader;
import static com.vtence.molecule.support.SetStatus.setStatus;
import static com.vtence.molecule.support.WriteBody.writeBody;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(JMock.class)
public class ApacheCommonLoggerTest {

    Mockery context = new JUnit4Mockery() {{ setImposteriser(ClassImposteriser.INSTANCE); }};
    Logger logger = context.mock(Logger.class);
    Application successor = context.mock(Application.class, "successor");
    Date currentTime = calendarDate(2012, 6, 27).atTime(12, 4, 0).inZone("GMT-05:00").build();
    ApacheCommonLogger apacheCommonLogger = new ApacheCommonLogger(logger, BrokenClock.stoppedAt(currentTime), TimeZone.getTimeZone("GMT+01:00"));

    MockRequest request = aRequest().withIp("192.168.0.1");
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor()  {
        apacheCommonLogger.connectTo(successor);
    }

    @Test public void
    logsRequestsServedInApacheCommonLogFormat() throws Exception {
        final String responseBody = "a response with a size of 28";
        context.checking(new Expectations() {{
            allowing(successor).handle(with(request), with(response)); will(doAll(writeBody(responseBody), setStatus(OK), setHeader("Content-Length", 28)));
            oneOf(logger).info(with("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28"));
        }});

        request.withMethod(GET).withPath("/products?keyword=dogs");
        apacheCommonLogger.handle(request, response);
    }

    @Test
    public void
    hyphenReplacesContentSizeForEmptyResponses() throws Exception {
        context.checking(new Expectations() {{
            allowing(successor).handle(with(request), with(response)); will(doAll(writeBody(""), setStatus(NO_CONTENT)));
            oneOf(logger).info(with(containsString("\"DELETE /logout HTTP/1.1\" 204 -")));
        }});

        request.withMethod(DELETE).withPath("/logout");
        apacheCommonLogger.handle(request, response);
    }
}
