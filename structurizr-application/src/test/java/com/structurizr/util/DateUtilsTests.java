package com.structurizr.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class DateUtilsTests {

    private DateFormat dateFormat;

    @BeforeEach
    void setUp() {
        dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone(DateUtils.UTC_TIME_ZONE));
    }

    @Test
    void test_getEndOfDay() {
        Date date = DateUtils.getEndOfDay(2015, 7, 14);

        Calendar today = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.UTC_TIME_ZONE));
        today.set(Calendar.YEAR, 2015);
        today.set(Calendar.MONTH, 6);
        today.set(Calendar.DAY_OF_MONTH, 14);
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 0);
        assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date));
    }

    @Test
    void test_getXDaysAgo_WhereXIsZero() {
        Date date = DateUtils.getXDaysAgo(0);

        Calendar today = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.UTC_TIME_ZONE));
        today.add(Calendar.DAY_OF_MONTH, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date));
   }

    @Test
    void test_getXDaysAgo_WhereXIsOne() {
        Date date = DateUtils.getXDaysAgo(1);

        Calendar today = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.UTC_TIME_ZONE));
        today.add(Calendar.DAY_OF_MONTH, -1);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date));
   }

    @Test
    void test_getDate() {
        Date date = DateUtils.getDate(2013, 7, 1, 17, 30, 00);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:SSS");
        sdf.setTimeZone(TimeZone.getTimeZone(DateUtils.UTC_TIME_ZONE));
        assertEquals("01-Jul-2013 17:30:00:000", sdf.format(date));
    }

    @Test
    void test_parseIsoDate() {
        try {
            DateUtils.parseIsoDate("2015-10-21T14:49:36Z");
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

}