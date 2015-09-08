package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.Clock;
import com.vtence.molecule.lib.SystemClock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ApacheCommonLogger extends AbstractMiddleware {

    private static final String COMMON_LOG_FORMAT = "%s - %s [%s] \"%s %s %s\" %s %s";
    private static final String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    private final Logger logger;
    private final Clock clock;
    private final Locale locale;
    private final TimeZone timeZone;

    public ApacheCommonLogger(Logger logger) {
        this(logger, new SystemClock());
    }

    public ApacheCommonLogger(Logger logger, Clock clock) {
        this(logger, clock, Locale.getDefault(), TimeZone.getDefault());
    }

    public ApacheCommonLogger(Logger logger, Clock clock, Locale locale, TimeZone timeZone) {
        this.logger = logger;
        this.clock = clock;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    public void handle(Request request, Response response) throws Exception {
        forward(request, response).whenSuccessful(logAccess(request));
    }

    private Consumer<Response> logAccess(Request request) {
        return response -> {
            String msg = String.format(COMMON_LOG_FORMAT,
                    request.remoteIp(),
                    "-",
                    currentTime(),
                    request.method(),
                    request.uri(),
                    request.protocol(),
                    response.statusCode(),
                    contentLengthOrHyphen(response));
            logger.info(msg);
        };
    }

    private String currentTime() {
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT, locale);
        formatter.setTimeZone(timeZone);
        return formatter.format(clock.now());
    }

    private Object contentLengthOrHyphen(Response response) {
        return response.size() > 0 ? response.size() : "-";
    }
}