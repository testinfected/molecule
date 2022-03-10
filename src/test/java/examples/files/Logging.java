package examples.files;

import java.io.OutputStream;
import java.util.logging.*;

import static java.util.logging.Level.OFF;

public class Logging {

    public static Logger off() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(OFF);
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