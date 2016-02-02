package com.vtence.molecule.http;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Parsing and formatting of HTTP dates as used in cookies and other headers.
 * <p>
 * This class handles dates as defined by RFC 2616 section 3.3.1
 */
public final class HttpDate {

    private static final DateTimeFormatter RFC_1123_FORMAT =
            DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US);
    private static final DateTimeFormatter RFC_1036_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd-MMM-yy HH:mm:ss zzz", Locale.US);
    private static final DateTimeFormatter ANSI_ASCTIME_FORMAT =
            DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.US);

    private static final DateTimeFormatter[] POSSIBLE_FORMATS = new DateTimeFormatter[] {
            RFC_1123_FORMAT,
            RFC_1036_FORMAT,
            ANSI_ASCTIME_FORMAT
    };
    private static final ZoneId GMT = ZoneId.of("GMT");

    public static Instant parse(String httpDate) {
        for (DateTimeFormatter format : POSSIBLE_FORMATS) {
            try {
                // Ignore timezone component as all HTTP dates should be represented in UTC
                return LocalDateTime.parse(httpDate, format).atZone(GMT).toInstant();
            } catch (DateTimeParseException skip) {
                // try next
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + httpDate);
    }

    public static String format(long epochMillis) {
        return httpDate(Instant.ofEpochMilli(epochMillis));
    }

    public static String httpDate(Instant pointInTime) {
        return rfc1123(pointInTime);
    }

    public static String rfc1123(Instant pointInTime) {
        return RFC_1123_FORMAT.format(ZonedDateTime.ofInstant(pointInTime, GMT));
    }

    HttpDate() {}
}
