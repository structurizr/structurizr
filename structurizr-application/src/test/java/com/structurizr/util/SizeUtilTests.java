package com.structurizr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SizeUtilTests {

    @Test
    void parse_ReturnsZero_WhenSizeIsEmpty() {
        assertEquals(0, SizeUtils.parse(""));
        assertEquals(0, SizeUtils.parse(" "));
        assertEquals(0, SizeUtils.parse(null));
    }

    @Test
    void parse_ThrowsAnException_WhenIncorrectPatternIsSpecified() {
        try {
            assertEquals(0, SizeUtils.parse("-1"));
        } catch (Exception e) {
            assertEquals("Size must be specified in bytes, MB, or KB - for example: 1048576, 1024KB, or 1MB",e.getMessage());
        }

        try {
            assertEquals(0, SizeUtils.parse("1GB"));
        } catch (Exception e) {
            assertEquals("Size must be specified in bytes, MB, or KB - for example: 1048576, 1024KB, or 1MB",e.getMessage());
        }

        try {
            assertEquals(0, SizeUtils.parse("1.5MB"));
        } catch (Exception e) {
            assertEquals("Size must be specified in bytes, MB, or KB - for example: 1048576, 1024KB, or 1MB",e.getMessage());
        }
    }

    @Test
    void parse_ReturnsBytes_WhenNoSuffixSpecified() {
        assertEquals(1048576, SizeUtils.parse("1048576"));
    }

    @Test
    void parse_ReturnsKilobytes_WhenKBSuffixSpecified() {
        assertEquals(1048576, SizeUtils.parse("1024KB"));
    }

    @Test
    void parse_ReturnsMegabytes_WhenMBSuffixSpecified() {
        assertEquals(1048576, SizeUtils.parse("1MB"));
    }

}
