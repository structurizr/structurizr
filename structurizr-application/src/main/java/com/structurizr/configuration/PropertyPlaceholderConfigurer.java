package com.structurizr.configuration;

import java.io.File;
import java.util.Properties;

public final class PropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {

    private static final String STRUCTURIZR_DATA_DIRECTORY_PROPERTY_NAME = "structurizr.dataDirectory";

    public PropertyPlaceholderConfigurer() {
        Properties properties = Configuration.getInstance().getProperties();
        File dataDirectory = Configuration.getInstance().getDataDirectory();
        properties.setProperty(STRUCTURIZR_DATA_DIRECTORY_PROPERTY_NAME, dataDirectory.getAbsolutePath());

        setLocalOverride(true);
        setProperties(properties);
        setIgnoreUnresolvablePlaceholders(false);
    }

}