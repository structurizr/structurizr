package com.structurizr.util;

public final class HtmlUtils {

    public static String filterHtml(String s) {
        if (s == null) {
            return null;
        }

        s = s.replaceAll("&lt;", "");
        s = s.replaceAll("&gt;", "");
        s = s.replaceAll("&nbsp;", "");
        s = s.replaceAll("(?s)<!--.*?-->", "");
        s = s.replaceAll("(?s)<[a-zA-Z]{1,10}.*?>", "");
        s = s.replaceAll("(?s)</[a-zA-Z]{1,10}>", "");

        return s;
    }

    public static String escapeQuoteCharacters(String s) {
        if (s == null) {
            return null;
        }

        s = s.replace("'", "\\'");

        return s;
    }
    
}