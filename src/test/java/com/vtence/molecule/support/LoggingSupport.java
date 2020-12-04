package com.vtence.molecule.support;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;

public class LoggingSupport {

    public static Logger anonymousLogger(Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }

    public static class LogRecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

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
            return records.stream().map(LogRecord::getMessage).collect(toList());
        }

        public void assertEntries(Matcher<? super List<String>> matching) {
            assertThat("log messages", messages(), matching);
        }
    }

}
