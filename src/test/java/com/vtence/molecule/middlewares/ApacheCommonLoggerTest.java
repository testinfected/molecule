package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Uri;
import com.vtence.molecule.support.LoggingSupport.LogRecordingHandler;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpStatus.NO_CONTENT;
import static com.vtence.molecule.support.LoggingSupport.anonymousLogger;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

public class ApacheCommonLoggerTest {

    LogRecordingHandler logRecords = new LogRecordingHandler();
    Instant currentTime = LocalDateTime.of(2012, 6, 27, 12, 4, 0)
                                       .toInstant(ZoneOffset.of("-05:00"));
    ApacheCommonLogger logger = new ApacheCommonLogger(anonymousLogger(logRecords),
                                                       Clock.fixed(currentTime, ZoneId.of("GMT+01:00")),
                                                       Locale.US);

    @Test
    public void
    logsRequestsServedInApacheCommonLogFormat() throws Exception {
        Response response = logger.then(request -> Response.ok()
                                                           .done("a response with a size of 28"))
                                  .handle(Request.get("/products?keyword=dogs")
                                                 .protocol("HTTP/1.1")
                                                 .remoteIp("192.168.0.1"));

        response.await();

        logRecords.assertEntries(contains("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28"));
    }

    @Test
    public void
    replacesContentSizeWithHyphenForEmptyOrChunkedResponses() throws Exception {
        Response response = logger.then(request -> Response.of(NO_CONTENT)
                                                           .done(""))
                                  .handle(Request.delete("/logout")
                                                 .protocol("HTTP/1.1")
                                                 .remoteIp("192.168.0.1"));

        response.await();

        logRecords.assertEntries(contains(containsString("\"DELETE /logout HTTP/1.1\" 204 -")));
    }

    @Test
    public void
    usesOriginalRequestValues() throws Exception {
        logger.then(request -> {
            request.uri(Uri.of("/changed"))
                   .method(POST)
                   .remoteIp("100.100.100.1")
                   .protocol("HTTPS");
            return Response.of(NO_CONTENT).done();
        }).handle(Request.delete("/logout")
                         .protocol("HTTP/1.1")
                         .remoteIp("192.168.0.1"));

        logRecords.assertEntries(contains(containsString("\"DELETE /logout HTTP/1.1\" 204 -")));
    }

}