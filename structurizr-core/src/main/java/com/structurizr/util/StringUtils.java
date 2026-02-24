package com.structurizr.util;

public final class StringUtils {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String truncate(String s, int length) {
        if (isNullOrEmpty(s)) {
            return "";
        }

        if (s.length() > length) {
            return s.substring(0, length-3) + "...";
        } else {
            return s;
        }
    }

}