package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HeaderNames;

import java.time.Clock;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ApacheCombinedFormatLogger extends ApacheLogger {
    private static final String COMBINED_LOG_FORMAT = "%s - - [%s] \"%s %s %s\" %s %s \"%s\" \"%s\"";

    public ApacheCombinedFormatLogger(Logger logger) {
        super(logger, Clock.systemDefaultZone());
    }

    public ApacheCombinedFormatLogger(Logger logger, Clock clock, Locale locale) {
        super(logger, clock, locale);
    }

    @Override
    protected Consumer<Response> logAccess(Request request) {
        return response -> {
            String msg = String.format(COMBINED_LOG_FORMAT,
                    request.remoteIp(),
                    currentTime(),
                    request.method(),
                    request.uri(),
                    request.protocol(),
                    response.statusCode(),
                    contentLengthOf(response),
                    nullToEmpty(request.header(HeaderNames.REFERER)),
                    nullToEmpty(request.header(HeaderNames.USER_AGENT)));
            logger.info(msg);
        };
    }
}
