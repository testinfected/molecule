package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ApacheCommonLogger extends AbstractMiddleware {

    private static final String COMMON_LOG_FORMAT = "%s - %s [%s] \"%s %s %s\" %s %s";
    private static final String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    private final Logger logger;
    private final Clock clock;
    private final DateTimeFormatter formatter;

    public ApacheCommonLogger(Logger logger) {
        this(logger, Clock.systemDefaultZone());
    }

    public ApacheCommonLogger(Logger logger, Clock clock) {
        this(logger, clock, Locale.getDefault());
    }

    public ApacheCommonLogger(Logger logger, Clock clock, Locale locale) {
        this.logger = logger;
        this.clock = clock;
        this.formatter = DateTimeFormatter.ofPattern(DATE_FORMAT, locale).withZone(clock.getZone());
    }

    public void handle(Request request, Response response) throws Exception {
        forward(request, response).whenSuccessful(logAccess(request));
    }

    private Consumer<Response> logAccess(Request request) {
        String remoteIp = request.remoteIp();
        HttpMethod method = request.method();
        String uri = request.uri();
        String protocol = request.protocol().toUpperCase();

        return response -> {
            String msg = String.format(COMMON_LOG_FORMAT,
                    remoteIp,
                    "-",
                    currentTime(),
                    method,
                    uri,
                    protocol,
                    response.statusCode(),
                    contentLengthOf(response));
            logger.info(msg);
        };
    }

    private String currentTime() {
        return ZonedDateTime.now(clock).format(formatter);
    }

    private Object contentLengthOf(Response response) {
        return response.size() > 0 ? response.size() : "-";
    }
}