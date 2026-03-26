package com.structurizr.command;

import com.structurizr.view.ColorScheme;

import java.io.File;

interface PlaywrightExporter {

    void run(String url, String format, ColorScheme colorScheme, boolean animation, File outputDir);

}