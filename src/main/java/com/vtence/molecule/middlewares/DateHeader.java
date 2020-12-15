package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;

import java.time.Clock;

import static com.vtence.molecule.http.HeaderNames.DATE;

public class DateHeader implements Middleware {

    private final Clock clock;

    public DateHeader(Clock clock) {
        this.clock = clock;
    }

    public Application then(Application next) {
        return request -> next.handle(request)
                              .whenSuccessful(this::setDateHeaderIfMissing);
    }

    private void setDateHeaderIfMissing(Response response) {
        if (!response.hasHeader(DATE)) {
            response.header(DATE, clock.instant());
        }
    }
}