package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.time.Clock;

import static com.vtence.molecule.http.HeaderNames.DATE;

public class DateHeader extends AbstractMiddleware {

    private final Clock clock;

    public DateHeader() {
        this(Clock.systemDefaultZone());
    }

    public DateHeader(Clock clock) {
        this.clock = clock;
    }

    public void handle(Request request, Response response) throws Exception {
        forward(request, response).whenSuccessful(this::setDateHeaderIfMissing);
    }

    public Application then(Application next) {
        return Application.of(request -> next.handle(request)
                                             .whenSuccessful(this::setDateHeaderIfMissing));
    }

    private void setDateHeaderIfMissing(Response response) {
        if (!response.hasHeader(DATE)) {
            response.header(DATE, clock.instant());
        }
    }
}