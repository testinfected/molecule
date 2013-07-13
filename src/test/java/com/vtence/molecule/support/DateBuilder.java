package com.vtence.molecule.support;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateBuilder {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private TimeZone timeZone = GMT;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int millisecond;

    public static DateBuilder aDate() {
        return new DateBuilder().fromMillis(System.currentTimeMillis());
    }

    public static DateBuilder calendarDate(int year, int month, int day) {
        return aDate().onCalendar(year, month, day).atMidnight();
    }

    public DateBuilder inZone(String zone) {
        return in(TimeZone.getTimeZone(zone));
    }

    public DateBuilder in(TimeZone zone) {
        timeZone = zone;
        return this;
    }

    public DateBuilder onCalendar(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        return this;
    }

    public DateBuilder atTime(int hour, int minute, int second) {
        return atTime(hour, minute, second, 0);
    }

    public DateBuilder atTime(int hour, int minute, int second, int millisecond) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
        return this;
    }

    public DateBuilder atMidnight() {
        return atTime(0, 0, 0);
    }

    public DateBuilder fromMillis(long millis) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(millis);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        millisecond = calendar.get(Calendar.MILLISECOND);
        return this;
    }

    public Date build() {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
    }
}