package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;

import java.time.Clock;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ApacheCommonLogger extends ApacheLogger {
    private static final String COMMON_LOG_FORMAT = "%s - %s [%s] \"%s %s %s\" %s %s";

    public ApacheCommonLogger(Logger logger, Clock clock, Locale locale) {
        super(logger, clock, locale);
    }

    @Override
    protected Consumer<Response> logAccess(Request request) {
        String remoteIp = request.remoteIp();
        HttpMethod method = request.method();
        String uri = request.uri().uri();
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
}