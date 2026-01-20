package com.structurizr.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Themes {

    private static Set<String> BUILT_IN_THEMES = new HashSet<>();

    public static void setBuiltInThemes(Collection<String> themes) {
        BUILT_IN_THEMES = new HashSet<>(themes);
    }

    public static boolean isBuiltIn(String theme) {
        return BUILT_IN_THEMES.contains(theme);
    }

}