package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.LoggingSupport.LogRecordingHandler;
import org.junit.Test;

import java.time.*;
import java.util.Locale;

import static com.vtence.molecule.http.HttpMethod.*;
import static com.vtence.molecule.http.HttpStatus.NO_CONTENT;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.support.LoggingSupport.anonymousLogger;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

public class ApacheCommonLoggerTest {
    LogRecordingHandler logRecords = new LogRecordingHandler();
    Instant currentTime = LocalDateTime.of(2012, 6, 27, 12, 4, 0).toInstant(ZoneOffset.of("-05:00"));
    ApacheCommonLogger apacheCommonLogger = new ApacheCommonLogger(anonymousLogger(logRecords),
            Clock.fixed(currentTime, ZoneId.of("GMT+01:00")), Locale.US);

    Request request = new Request().protocol("HTTP/1.1").remoteIp("192.168.0.1");
    Response response = new Response();

    @Test
    public void
    logsRequestsServedInApacheCommonLogFormat() throws Exception {
        request.method(GET).uri("/products?keyword=dogs");

        apacheCommonLogger.handle(request, response);
        response.status(OK).body("a response with a size of 28").done();

        response.await();
        logRecords.assertEntries(contains("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28"));
    }

    @Test
    public void
    replacesContentSizeWithHyphenForEmptyOrChunkedResponses() throws Exception {
        request.remoteIp("192.168.0.1").method(DELETE).uri("/logout");

        apacheCommonLogger.handle(request, response);
        response.status(NO_CONTENT).body("").done();

        response.await();
        logRecords.assertEntries(contains(containsString("\"DELETE /logout HTTP/1.1\" 204 -")));
    }

    @Test
    public void
    usesOriginalRequestValues() throws Exception {
        request.remoteIp("192.168.0.1").method(DELETE).uri("/logout");

        apacheCommonLogger.handle(request, response);
        request.uri("/changed").method(POST).remoteIp("100.100.100.1").protocol("HTTPS");
        response.status(NO_CONTENT).done();

        response.await();
        logRecords.assertEntries(contains(containsString("\"DELETE /logout HTTP/1.1\" 204 -")));
    }

}