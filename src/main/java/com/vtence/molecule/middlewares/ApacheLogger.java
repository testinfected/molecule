package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class ApacheLogger extends AbstractMiddleware {
    private static final String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    protected final Logger logger;
    private final Clock clock;
    private final DateTimeFormatter formatter;

    protected ApacheLogger(Logger logger) {
        this(logger, Clock.systemDefaultZone(), Locale.getDefault());
    }

    protected ApacheLogger(Logger logger, Clock clock, Locale locale) {
        this.logger = logger;
        this.clock = clock;
        this.formatter = DateTimeFormatter.ofPattern(DATE_FORMAT, locale).withZone(clock.getZone());
    }

    public Application then(Application next) {
        return Application.of(request -> {
            Consumer<Response> logAccess = logAccess(request);
            return next.handle(request).whenSuccessful(logAccess);
        });
    }

    public void handle(Request request, Response response) throws Exception {
        // Capture original request values
        Consumer<Response> logAccess = logAccess(request);
        forward(request, response).whenSuccessful(logAccess);
    }

    protected abstract Consumer<Response> logAccess(Request request);

    protected String nullToEmpty(String string) {
        return (string == null) ? "" : string;
    }

    protected String currentTime() {
        return ZonedDateTime.now(clock).format(formatter);
    }

    protected Object contentLengthOf(Response response) {
        return response.size() > 0 ? response.size() : "-";
    }
}
