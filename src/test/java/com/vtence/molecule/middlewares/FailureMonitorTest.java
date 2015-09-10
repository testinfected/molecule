package com.vtence.molecule.middlewares;

import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class FailureMonitorTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    FailureReporter failureReporter = context.mock(FailureReporter.class);
    FailureMonitor monitor = new FailureMonitor(failureReporter);

    @Rule
    public ExpectedException error = ExpectedException.none();

    Request request = new Request();
    Response response = new Response();

    @Test
    public void notifiesFailureReporterAndRethrowsExceptionInCaseOfError() throws Exception {
        monitor.connectTo((request, response) -> {
            throw new Exception("Crash!");
        });

        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(with(exceptionWithMessage("Crash!")));
        }});

        error.expectMessage("Crash!");
        monitor.handle(request, response);
    }

    @Test
    public void doesNothingWhenResponseCompletesNormally() throws Exception {
        context.checking(new Expectations() {{
            never(failureReporter);
        }});

        monitor.handle(request, response);
        response.done();

        assertNoExecutionError();
    }

    @Test
    public void notifiesFailureReporterWhenErrorOccursLater() throws Exception {
        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(with(exceptionWithMessage("Crash!")));
        }});

        monitor.handle(request, response);
        response.done(new Exception("Crash!"));
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }

    private Matcher<Exception> exceptionWithMessage(String message) {
        return hasProperty("message", equalTo(message));
    }
}