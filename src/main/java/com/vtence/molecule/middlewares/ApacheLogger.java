package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class ApacheLogger implements Middleware {
    private static final String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    protected final Logger logger;
    private final Clock clock;
    private final DateTimeFormatter formatter;

    protected ApacheLogger(Logger logger, Clock clock, Locale locale) {
        this.logger = logger;
        this.clock = clock;
        this.formatter = DateTimeFormatter.ofPattern(DATE_FORMAT, locale).withZone(clock.getZone());
    }

    public Application then(Application next) {
        return request -> {
            Consumer<Response> logAccess = logAccess(request);
            return next.handle(request).whenSuccessful(logAccess);
        };
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
