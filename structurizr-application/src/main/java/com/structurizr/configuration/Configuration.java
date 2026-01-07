package com.structurizr.configuration;

import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.http.HttpClient;
import com.structurizr.util.StringUtils;
import com.structurizr.util.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static com.structurizr.configuration.StructurizrProperties.*;

public class Configuration {

    private static final Log log = LogFactory.getLog(Configuration.class);

    private static final String COMMA = ",";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public static final String DEFAULT_STRUCTURIZR_DATA_DIRECTORY = "/usr/local/structurizr";
    private static final String WORK_DIRECTORY_NAME = ".structurizr";

    private static final String PROPERTY_NAME_PREFIX = "structurizr.";
    private static final String ENVIRONMENT_VARIABLE_NAME_PREFIX = "structurizr_";

    private final Profile profile;
    private final Properties properties;
    private final com.structurizr.util.Features features = new com.structurizr.util.Features();

    private String webUrl = "";

    private Set<String> adminUsersAndRoles = new HashSet<>();

    private static Configuration INSTANCE;

    static {
        initLogger();
    }

    private Configuration(Profile profile, Properties properties) {
        this.profile = profile;
        this.properties = properties;

        setDefault(DATA_DIRECTORY, DEFAULT_DATA_DIRECTORY_PATH);

        loadProperties();
        loadSystemProperties();
        loadEnvironmentVariables();
        setDefaults();

        if (profile == Profile.Server) {
            configureFeatures();
            setAdminUsersAndRoles(getProperty(ADMIN_USERS_AND_ROLES).split(COMMA));
        }

        if (!getDataDirectory().exists()) {
            getDataDirectory().mkdirs();
        }

        if (!getWorkDirectory().exists()) {
            getWorkDirectory().mkdirs();
        }
    }

    public static Configuration init(Profile profile, Properties properties) {
        INSTANCE = new Configuration(profile, properties);

        return INSTANCE;
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getProperty(String structurizrPropertyName) {
        String value = null;

        structurizrPropertyName = structurizrPropertyName.toLowerCase();
        if (properties.containsKey(structurizrPropertyName)) {
            value = properties.getProperty(structurizrPropertyName);
        }

        // translate ${...} into a value from the named environment variable
        // (this mirrors what Spring does via the property placeholders)
        if (value != null) {
            if (value.startsWith("${") && value.endsWith("}")) {
                String environmentVariableName = value.substring(2, value.length()-1);
                value = System.getenv(environmentVariableName);
            }
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    public void setDefault(String name, String defaultValue) {
        name = name.toLowerCase();
        if (!properties.containsKey(name)) {
            properties.setProperty(name, defaultValue);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isAuthenticationEnabled() {
        return !AUTHENTICATION_VARIANT_NONE.equals(getProperty(AUTHENTICATION_IMPLEMENTATION));
    }

    public void setFeatureEnabled(String feature) {
        features.enable(feature);
    }

    public void setFeatureDisabled(String feature) {
        features.disable(feature);
    }

    public boolean isFeatureEnabled(String feature) {
        return features.isEnabled(feature);
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String url) {
        if (url != null) {
            if (url.endsWith("/")) {
                this.webUrl = url.substring(0, url.length()-1);
            } else {
                this.webUrl = url;
            }
        }
    }

    public String getApiUrl() {
        return webUrl + "/api";
    }

    public boolean isSafeMode() {
        return false;
    }

    public Set<String> getAdminUsersAndRoles() {
        return new HashSet<>(adminUsersAndRoles);
    }

    private void setAdminUsersAndRoles(String... usersAndRoles) {
        adminUsersAndRoles = new HashSet<>();
        if (usersAndRoles != null) {
            for (String userOrRole : usersAndRoles) {
                if (!StringUtils.isNullOrEmpty(userOrRole)) {
                    adminUsersAndRoles.add(userOrRole.trim().toLowerCase());
                }
            }
        }
    }

    public File getDataDirectory() {
        return new File(getProperty(DATA_DIRECTORY));
    }

    public boolean isSingleWorkspace() {
        return getProperty(WORKSPACES_PROPERTY).equals(SINGLE_WORKSPACE);
    }

    public File getWorkDirectory() {
        return new File(getDataDirectory(), WORK_DIRECTORY_NAME);
    }

    private void loadProperties() {
        File file = new File(getDataDirectory(), StructurizrProperties.CONFIGURATION_FILE_NAME);
        Properties propertiesFromFile = new Properties();
        try {
            if (file.exists()) {
                propertiesFromFile.load(new FileReader(file));

                for (String key : propertiesFromFile.stringPropertyNames()) {
                    String name = key.toLowerCase();
                    if (name.startsWith(PROPERTY_NAME_PREFIX)) {
                        properties.setProperty(name, propertiesFromFile.getProperty(key));
                    } else {
                        properties.setProperty(PROPERTY_NAME_PREFIX + name, propertiesFromFile.getProperty(key));
                    }
                }

            }
        } catch (IOException e) {
            log.warn(e);
        }
    }

    private void loadSystemProperties() {
        for (String name : System.getProperties().stringPropertyNames()) {
            String lowerCaseName = name.toLowerCase();
            if (lowerCaseName.startsWith(PROPERTY_NAME_PREFIX)) {
                properties.setProperty(lowerCaseName, System.getProperty(name));
            }
        }
    }

    private void loadEnvironmentVariables() {
        for (String name : System.getenv().keySet()) {
            String lowerCaseName = name.toLowerCase();
            if (lowerCaseName.startsWith(ENVIRONMENT_VARIABLE_NAME_PREFIX)) {
                lowerCaseName = lowerCaseName.replace('_', '.');
                properties.setProperty(lowerCaseName, System.getenv(name));
            }
        }
    }

    private void setDefaults() {
        setDefault(NETWORK_TIMEOUT, DEFAULT_NETWORK_TIMEOUT_OF_SIXTY_SECONDS);

        if (profile != Profile.Playground) {
            setDefault(AUTHENTICATION_IMPLEMENTATION, AUTHENTICATION_VARIANT_NONE);
            setDefault(DATA_STORAGE_IMPLEMENTATION, DATA_STORAGE_VARIANT_FILE);
            setDefault(SEARCH_IMPLEMENTATION, SEARCH_VARIANT_LUCENE);
            setDefault(CACHE_IMPLEMENTATION, CACHE_VARIANT_NONE);
            setDefault(WORKSPACE_THREADS, DEFAULT_NUMBER_OF_THREADS);
            setDefault(NETWORK_URLS_ALLOWED, ".*");
        }

        if (profile == Profile.Local) {
            setDefault(EDITABLE_PROPERTY, TRUE);
            setDefault(WORKSPACES_PROPERTY, SINGLE_WORKSPACE);
            setDefault(AUTO_SAVE_INTERVAL_PROPERTY, DEFAULT_AUTO_SAVE_INTERVAL_IN_MILLISECONDS);
            setDefault(AUTO_REFRESH_INTERVAL_PROPERTY, DEFAULT_AUTO_REFRESH_INTERVAL_IN_MILLISECONDS);
        } else if (profile == Profile.Server) {
            setDefault(SESSION_IMPLEMENTATION, SESSION_VARIANT_LOCAL);
            setDefault(URL, "");
            setDefault(API_KEY, "");
            setDefault(MAX_WORKSPACE_VERSIONS, DEFAULT_MAX_WORKSPACE_VERSIONS);
            setDefault(CACHE_EXPIRY_IN_MINUTES, DEFAULT_CACHE_EXPIRY_IN_MINUTES);
            setDefault(ADMIN_USERS_AND_ROLES, "");
            setDefault(WORKSPACE_EVENT_LISTENER_PLUGIN, "");
            setDefault(DSL_EDITOR, FALSE); // backwards compatibility

            setDefault(Features.UI_DSL_EDITOR, FALSE);
            setDefault(Features.WORKSPACE_ARCHIVING, FALSE);
            setDefault(Features.WORKSPACE_BRANCHES, FALSE);
            setDefault(Features.WORKSPACE_SCOPE_VALIDATION, Features.WORKSPACE_SCOPE_VALIDATION_RELAXED);
            setDefault(Features.DIAGRAM_REVIEWS, TRUE);
            setDefault(Features.DIAGRAM_ANONYMOUS_THUMBNAILS, FALSE);
        }
    }

    private static void initLogger() {
        class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

            private Log log = LogFactory.getLog(UncaughtExceptionHandler.class);

            public void uncaughtException(Thread t, Throwable ex) {
                log.error("Uncaught exception in thread: " + t.getName(), ex);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    private void configureFeatures() {
        features.configure(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getProperty(Features.UI_DSL_EDITOR)));
        features.configure(Features.WORKSPACE_ARCHIVING, Boolean.parseBoolean(getProperty(Features.WORKSPACE_ARCHIVING)));
        features.configure(Features.WORKSPACE_BRANCHES, Boolean.parseBoolean(getProperty(Features.WORKSPACE_BRANCHES)));
        features.configure(Features.WORKSPACE_SCOPE_VALIDATION, getProperty(Features.WORKSPACE_SCOPE_VALIDATION).equalsIgnoreCase(Features.WORKSPACE_SCOPE_VALIDATION_STRICT));
        features.configure(Features.DIAGRAM_REVIEWS, Boolean.parseBoolean(getProperty(Features.DIAGRAM_REVIEWS)));
        features.configure(Features.DIAGRAM_ANONYMOUS_THUMBNAILS, Boolean.parseBoolean(getProperty(Features.DIAGRAM_ANONYMOUS_THUMBNAILS)));

        String search = getProperty(SEARCH_IMPLEMENTATION);
        features.configure(Features.WORKSPACE_SEARCH, !search.equals(SEARCH_VARIANT_NONE));

        // for backwards compatibility (older versions had structurizr.dslEditor=true)
        if (!isFeatureEnabled(Features.UI_DSL_EDITOR)) {
            features.configure(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getProperty(DSL_EDITOR)));
        }

        properties.remove(DSL_EDITOR); // not needed after the feature has been configured
    }

    public StructurizrDslParser createStructurizrDslParser() {
        StructurizrDslParser parser = new StructurizrDslParser();

        parser.getFeatures().configure(com.structurizr.dsl.Features.ENVIRONMENT, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.FILE_SYSTEM, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.PLUGINS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.SCRIPTS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.COMPONENT_FINDER, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DOCUMENTATION, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DECISIONS, false);

        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith("structurizr.feature.dsl.")) {
                parser.getFeatures().configure(name, Boolean.parseBoolean(getProperty(name)));
            }
        }

        configure(parser.getHttpClient());

        return parser;
    }

    public HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();
        configure(httpClient);

        return httpClient;
    }

    public void configure(HttpClient httpClient) {
        String urlsAllowed = getProperty(NETWORK_URLS_ALLOWED);
        if (!StringUtils.isNullOrEmpty(urlsAllowed)) {
            String[] regexes = urlsAllowed.split(",");
            for (String regex : regexes) {
                httpClient.allow(regex.trim());
            }
        }

        int timeoutInMilliseconds = Integer.parseInt(getProperty(NETWORK_TIMEOUT));
        httpClient.setTimeout(timeoutInMilliseconds);
    }

    public void banner(Class<?> clazz) {
        Log log = LogFactory.getLog(clazz);

        log.info("***********************************************************************************");
        log.info("  _____ _                   _              _          ");
        log.info(" / ____| |                 | |            (_)         ");
        log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
        log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
        log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
        log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
        log.info("                                                      ");
        log.info("v" + new Version().getBuildNumber());

        logAllProperties(log, getProperties());
        log.info("***********************************************************************************");
    }

    private void logAllProperties(Log log, Properties properties) {
        log.info("***********************************************************************************");

        String propertiesToMask = ".*encryption|.*key|.*password|.*license";

        Set<String> propertyNames = new TreeSet<>(properties.stringPropertyNames());
        for (String name : propertyNames) {
            String value = properties.getProperty(name);
            if (name.toLowerCase().matches(propertiesToMask)) {
                if (!StringUtils.isNullOrEmpty(value)) {
                    log.info(" - " + name + ": ********");
                } else {
                    log.info(" - " + name + ":");
                }
            } else {
                log.info(" - " + name + ": " + value);
            }
        }
    }

}