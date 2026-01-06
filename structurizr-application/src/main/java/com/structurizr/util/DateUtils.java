package com.structurizr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utilities for calculating various points in time.
 */
public class DateUtils {

    public static final String UTC_TIME_ZONE = "UTC";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String CAL_VER_DATE_FORMAT = "yyyy.MM.dd";
    public static final String USER_FRIENDLY_DATE_FORMAT = "EEE dd MMM yyyy HH:mm z";

    public static Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone(UTC_TIME_ZONE));
    }

    public static Date getNow() {
        return getCalendar().getTime();
    }

    public static Date getXMinutesAgo(int numberOfMinutes) {
        Calendar cal = getCalendar();
        cal.add(Calendar.MINUTE, -numberOfMinutes);

        return cal.getTime();
    }

    public static boolean isOlderThanXMinutes(Date date, int numberOfMinutes) {
        return date.before(getXMinutesAgo(numberOfMinutes));
    }

    public static Date removeMilliseconds(Date date) {
        Calendar cal = getCalendar();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Date parseIsoDate(String s) throws ParseException {
        if (StringUtils.isNullOrEmpty(s)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

        return sdf.parse(s);
    }

    public static Date parseCalVerDate(String s) throws ParseException {
        if (StringUtils.isNullOrEmpty(s)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(CAL_VER_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

        return sdf.parse(s);
    }

    public static String formatIsoDate(Date d) {
        if (d != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

            return sdf.format(d);
        } else {
            return "";
        }
    }

    public static String formatDate(Date d) {
        if (d != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(USER_FRIENDLY_DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

            return sdf.format(d);
        } else {
            return "";
        }
    }

    public static Date getEndOfDay(int year, int month, int day) {
        return getDate(year, month, day, 23, 59, 59);
    }

    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

}