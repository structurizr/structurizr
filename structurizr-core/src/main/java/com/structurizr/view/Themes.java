package com.structurizr.view;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Themes {

    static final Map<String, File> THEMES = new HashMap<>();

    public static Collection<String> getThemes() {
        return THEMES.keySet();
    }

    public static File getTheme(String name) {
        return THEMES.get(name);
    }

    public static boolean isRegistered(String theme) {
        return THEMES.containsKey(theme);
    }

}