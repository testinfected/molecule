package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.FailureReporter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(JMock.class)
public class FailureMonitorTest {

    Mockery context = new JUnit4Mockery();
    FailureReporter failureReporter = context.mock(FailureReporter.class);
    FailureMonitor monitor = new FailureMonitor(failureReporter);
    Application successor = context.mock(Application.class, "successor");

    Exception error = new Exception("An internal error occurred!");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainToSuccessor() {
        monitor.connectTo(successor);
    }

    @Test public void
    notifiesFailureReporterAndRethrowsExceptionInCaseOfError() throws Exception {
        context.checking(new Expectations() {{
            allowing(successor).handle(with(request), with(response)); will(throwException(error));
            oneOf(failureReporter).errorOccurred(with(same(error)));
        }});

        try {
            monitor.handle(request, response);
            fail("Exception did not bubble up");
        } catch (Exception e) {
            assertThat("error", e, sameInstance(error));
        }
    }
}
