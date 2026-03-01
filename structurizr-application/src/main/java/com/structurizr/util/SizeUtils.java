package com.structurizr.util;

public class SizeUtils {

    private static final String KILOBYTE_SUFFIX = "kb";
    private static final String MEGABYTE_SUFFIX = "mb";

    private static final String PATTERN = "\\d+(KB|MB)??";

    public static int parse(String size) {
        if (StringUtils.isNullOrEmpty(size)) {
            return 0;
        }

        if (!size.matches(PATTERN)) {
            throw new IllegalArgumentException("Size must be specified in bytes, MB, or KB - for example: 1048576, 1024KB, or 1MB");
        }

        size = size.toLowerCase();

        if (size.endsWith(KILOBYTE_SUFFIX)) {
            return Integer.parseInt(size.substring(0, size.length() - KILOBYTE_SUFFIX.length())) * 1024;
        } else if (size.endsWith(MEGABYTE_SUFFIX)) {
            return Integer.parseInt(size.substring(0, size.length() - MEGABYTE_SUFFIX.length())) * 1024 * 1024;
        } else {
            return Integer.parseInt(size);
        }
    }

}