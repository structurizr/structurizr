package com.structurizr.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtils {

    private static final Log log = LogFactory.getLog(FileUtils.class);

    public static void write(File file, String content) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static void delete(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    delete(file);
                }
                file.delete();
            }
        }
    }

}