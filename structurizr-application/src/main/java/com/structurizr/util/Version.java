package com.structurizr.util;

import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static final String APP_VERSION_KEY = "app.version";

    private static String version = "";

    static {
        try {
            Properties properties = new Properties();
            InputStream in = Version.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(in);
            if (in != null) {
                version = properties.getProperty(APP_VERSION_KEY);
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBuildNumber() {
        return version;
    }

}