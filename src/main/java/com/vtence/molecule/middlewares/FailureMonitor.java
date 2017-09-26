package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Middleware;

public class FailureMonitor implements Middleware {
    private final FailureReporter reporter;

    public FailureMonitor(FailureReporter reporter) {
        this.reporter = reporter;
    }

    public Application then(Application next) {
        return Application.of(request -> {
            try {
                return next.handle(request).whenFailed((result, error) -> reporter.errorOccurred(error));
            } catch (Throwable error) {
                reporter.errorOccurred(error);
                throw error;
            }
        });
    }
}