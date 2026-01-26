package com.structurizr.util;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.model.Element;
import com.structurizr.view.ElementStyle;
import com.structurizr.view.Theme;
import com.structurizr.view.ThemeUtils;
import com.structurizr.view.Themes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Inlines built-in theme icons into the workspace.
 */
public final class BuiltInThemes {

    private static final Log log = LogFactory.getLog(BuiltInThemes.class);

    private static final String THEMES_DIRECTORY = "themes";
    private static final String THEME_JSON = "theme.json";

    public static List<String> getThemes() {
        List<String> themeNames = new ArrayList<>();

        File themesDirectory = new File(Configuration.getInstance().getDataDirectory(), THEMES_DIRECTORY);
        File[] themes = themesDirectory.listFiles();
        if (themes != null) {
            for (File theme : themes) {
                if (theme.isDirectory()) {
                    File themeJson = new File(theme, THEME_JSON);
                    if (themeJson.exists()) {
                        themeNames.add(theme.getName());
                    }
                }
            }
        }

        Collections.sort(themeNames);
        return themeNames;
    }

    public static void inlineIcons(Workspace workspace) {
        try {
            Set<String> elementTags = new HashSet<>();
            for (Element element : workspace.getModel().getElements()) {
                elementTags.addAll(element.getTagsAsSet());
            }

            for (String theme : workspace.getViews().getConfiguration().getThemes()) {
                if (Themes.isBuiltIn(theme)) {
                    Theme builtInTheme = loadBuiltInTheme(theme);

                    if (builtInTheme != null) {
                        for (ElementStyle styleInTheme : builtInTheme.getElements()) {
                            if (elementTags.contains(styleInTheme.getTag())) {
                                ElementStyle styleInWorkspace = workspace.getViews().getConfiguration().getStyles().getElementStyle(styleInTheme.getTag());
                                if (styleInWorkspace == null) {
                                    styleInWorkspace = workspace.getViews().getConfiguration().getStyles().addElementStyle(styleInTheme.getTag());
                                }

                                if (StringUtils.isNullOrEmpty(styleInWorkspace.getIcon())) {
                                    styleInWorkspace.setIcon(pngToDataUri(theme, styleInTheme.getIcon()));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not inline icons from built-in themes", e);
        }
    }

    private static Theme loadBuiltInTheme(String name) {
        try {
            InputStream inputStream = BuiltInThemes.class.getResourceAsStream("/static/static/themes/" + name + "/theme.json");
            if (inputStream != null) {
                String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return ThemeUtils.fromJson(json);
            } else {
                log.error("Could not load built-in theme " + name);
            }
        } catch (Exception e) {
            log.error("Could not load built-in theme " + name, e);
        }

        return null;
    }

    private static String pngToDataUri(String theme, String icon) {
        try {
            InputStream inputStream = BuiltInThemes.class.getResourceAsStream("/static/static/themes/" + theme + "/" + icon);
            if (inputStream != null) {
                byte[] png = inputStream.readAllBytes();
                return ImageUtils.getPngAsDataUri(png);
            } else {
                log.error("Could not load built-in theme icon " + icon);
            }
        } catch (Exception e) {
            log.error("Could not load built-in theme icon " + icon, e);
        }

        return null;
    }

}