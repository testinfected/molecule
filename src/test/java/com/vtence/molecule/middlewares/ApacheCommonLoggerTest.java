package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.BrokenClock;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.vtence.molecule.http.HttpMethod.DELETE;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpStatus.NO_CONTENT;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.support.Dates.calendarDate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ApacheCommonLoggerTest {
    LogRecordingHandler logRecords = new LogRecordingHandler();
    Date currentTime = calendarDate(2012, 6, 27).atTime(12, 4, 0).inZone("GMT-05:00").toDate();
    ApacheCommonLogger apacheCommonLogger =
            new ApacheCommonLogger(anonymousLogger(logRecords),
                    BrokenClock.stoppedAt(currentTime),
                    Locale.US, TimeZone.getTimeZone("GMT+01:00"));

    Request request = new Request().protocol("HTTP/1.1").remoteIp("192.168.0.1");
    Response response = new Response();

    @Test public void
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

    private Logger anonymousLogger(Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }

    public static class LogRecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<LogRecord>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public List<String> messages() {
            List<String> result = new ArrayList<String>();
            for (LogRecord record : records) {
                result.add(record.getMessage());
            }
            return result;
        }

        public void assertEntries(Matcher<? super List<String>> matching) {
            assertThat("log messages", messages(), matching);
        }
    }
}