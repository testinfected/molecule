package com.vtence.molecule.middlewares;

import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

public class FailureMonitor extends AbstractMiddleware {
    private final FailureReporter reporter;

    public FailureMonitor(FailureReporter reporter) {
        this.reporter = reporter;
    }

    public void handle(Request request, Response response) throws Exception {
        try {
            forward(request, response).whenFailed((result, error) -> reporter.errorOccurred(error));
        } catch (Throwable error) {
            reporter.errorOccurred(error);
            throw error;
        }
    }
}