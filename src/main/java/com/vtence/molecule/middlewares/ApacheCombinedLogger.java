package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpMethod;

import java.time.Clock;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ApacheCombinedLogger extends ApacheLogger {
    private static final String COMBINED_LOG_FORMAT = "%s - - [%s] \"%s %s %s\" %s %s \"%s\" \"%s\"";

    public ApacheCombinedLogger(Logger logger) {
        super(logger);
    }

    public ApacheCombinedLogger(Logger logger, Clock clock, Locale locale) {
        super(logger, clock, locale);
    }

    @Override
    protected Consumer<Response> logAccess(Request request) {
        String remoteIp = request.remoteIp();
        HttpMethod method = request.method();
        String uri = request.uri().uri();
        String protocol = request.protocol();
        String referer = request.header(HeaderNames.REFERER);
        String userAgent = request.header(HeaderNames.USER_AGENT);

        return response -> {
            String msg = String.format(COMBINED_LOG_FORMAT,
                    remoteIp,
                    currentTime(),
                    method,
                    uri,
                    protocol,
                    response.statusCode(),
                    contentLengthOf(response),
                    nullToEmpty(referer),
                    nullToEmpty(userAgent));
            logger.info(msg);
        };
    }
}
