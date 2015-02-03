package examples.files;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class Logging {

    public static Logger off() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.OFF);
        return logger;
    }

    public static Logger toConsole() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(ConsoleHandler.toStandardOutput());
        return logger;
    }

    public static class ConsoleHandler extends StreamHandler {
        public static ConsoleHandler toStandardOutput() {
            return new ConsoleHandler(System.out);
        }

        public ConsoleHandler(OutputStream out) {
            super(out, new PlainFormatter());
        }

        public void publish(LogRecord record) {
            super.publish(record);
            flush();
        }

        public void close() throws SecurityException {
            flush();
        }
    }

    public static class PlainFormatter extends Formatter {
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }
}