package com.structurizr.configuration;

public class StructurizrProperties {

    public static final String DATA_DIRECTORY = "structurizr.datadirectory";
    public static final String CONFIGURATION_FILE_NAME = "structurizr.properties";

    public static final String WORKSPACE_FILENAME = "structurizr.workspace.filename";

    public static final String WORKSPACE_THREADS = "structurizr.workspace.threads";

    public static final String WORKSPACE_EVENT_LISTENER_PLUGIN = "structurizr.plugin.workspaceeventlistener";

    public static final String LICENSE = "structurizr.license";
    public static final String URL = "structurizr.url";
    public static final String ENCRYPTION_PASSPHRASE = "structurizr.encryption";
    public static final String API_KEY = "structurizr.apikey";
    public static final String ADMIN_USERS_AND_ROLES = "structurizr.admin";
    public static final String MAX_WORKSPACE_VERSIONS = "structurizr.maxworkspaceversions";
    public static final String DSL_EDITOR = "structurizr.dsleditor";
    public static final String NETWORK_URLS_ALLOWED = "structurizr.network.urls.allowed";
    public static final String NETWORK_TIMEOUT = "structurizr.network.timeout";

    public static final String AUTHENTICATION_IMPLEMENTATION = "structurizr.authentication";
    public static final String AUTHENTICATION_VARIANT_NONE = "none";
    public static final String AUTHENTICATION_VARIANT_FILE = "file";
    public static final String AUTHENTICATION_VARIANT_SAML = "saml";

    public static final String SAML_REGISTRATION_ID = "structurizr.saml.registrationid";
    public static final String SAML_ENTITY_ID = "structurizr.saml.entityid";
    public static final String SAML_METADATA = "structurizr.saml.metadata";
    public static final String SAML_SIGNING_CERTIFICATE = "structurizr.saml.signing.certificate";
    public static final String SAML_SIGNING_PRIVATE_KEY = "structurizr.saml.signing.privatekey";
    public static final String SAML_ATTRIBUTE_USERNAME = "structurizr.saml.attribute.username";
    public static final String SAML_ATTRIBUTE_ROLE = "structurizr.saml.attribute.role";

    public static final String SESSION_IMPLEMENTATION = "structurizr.session";
    public static final String SESSION_VARIANT_LOCAL = "local";
    public static final String SESSION_VARIANT_REDIS = "redis";

    public static final String DEFAULT_DATA_DIRECTORY_PATH = "/usr/local/structurizr";
    public static final String DEFAULT_FILENAME = "workspace";
    public static final String DEFAULT_MAX_WORKSPACE_VERSIONS = "30";
    public static final String DEFAULT_NUMBER_OF_THREADS = "10";

    public static final String DEFAULT_NETWORK_TIMEOUT_OF_SIXTY_SECONDS = "" + (1000 * 60);

    public static final String DATA_STORAGE_IMPLEMENTATION = "structurizr.data";
    public static final String DATA_STORAGE_VARIANT_FILE = "file";
    public static final String DATA_STORAGE_VARIANT_AMAZON_WEB_SERVICES_S3 = "aws-s3";
    public static final String AWS_S3_ACCESS_KEY_ID = "structurizr.aws-s3.accesskeyid";
    public static final String AWS_S3_SECRET_ACCESS_KEY = "structurizr.aws-s3.secretaccesskey";
    public static final String AWS_S3_REGION = "structurizr.aws-s3.region";
    public static final String AWS_S3_BUCKET_NAME = "structurizr.aws-s3.bucketname";
    public static final String AWS_S3_ENDPOINT = "structurizr.aws-s3.endpoint";
    public static final String AWS_S3_PATH_STYLE_ACCESS = "structurizr.aws-s3.pathstyleaccess";
    public static final String DATA_STORAGE_VARIANT_AZURE_BLOB_STORAGE = "azure-blob";
    public static final String AZURE_BLOB_STORAGE_ACCOUNT_NAME = "structurizr.azure-blob.accountname";
    public static final String AZURE_BLOB_STORAGE_ACCESS_KEY = "structurizr.azure-blob.accesskey";
    public static final String AZURE_BLOB_STORAGE_CONTAINER_NAME = "structurizr.azure-blob.containername";

    public static final String SEARCH_IMPLEMENTATION = "structurizr.search";
    public static final String SEARCH_VARIANT_NONE = "none";
    public static final String SEARCH_VARIANT_LUCENE = "lucene";

    public static final String SEARCH_VARIANT_ELASTICSEARCH = "elasticsearch";
    public static final String ELASTICSEARCH_ENDPOINT = "structurizr.elasticsearch.endpoint";
    public static final String ELASTICSEARCH_PROTOCOL = "structurizr.elasticsearch.protocol";
    public static final String ELASTICSEARCH_HOST = "structurizr.elasticsearch.host";
    public static final String ELASTICSEARCH_PORT = "structurizr.elasticsearch.port";
    public static final String ELASTICSEARCH_USERNAME = "structurizr.elasticsearch.username";
    public static final String ELASTICSEARCH_PASSWORD = "structurizr.elasticsearch.password";

    public static final String CACHE_IMPLEMENTATION = "structurizr.cache";
    public static final String CACHE_EXPIRY_IN_MINUTES = "structurizr.cache.expiry";
    public static final String DEFAULT_CACHE_EXPIRY_IN_MINUTES = "5";
    public static final String CACHE_VARIANT_NONE = "none";
    public static final String CACHE_VARIANT_LOCAL = "local";
    public static final String CACHE_VARIANT_REDIS = "redis";

    public static final String REDIS_ENDPOINT = "structurizr.redis.endpoint";
    public static final String REDIS_PROTOCOL = "structurizr.redis.protocol";
    public static final String REDIS_SSL = "structurizr.redis.ssl";
    public static final String REDIS_HOST = "structurizr.redis.host";
    public static final String REDIS_PORT = "structurizr.redis.port";
    public static final String REDIS_PASSWORD = "structurizr.redis.password";
    public static final String REDIS_DATABASE = "structurizr.redis.database";

    public static final String WORKSPACES_PROPERTY = "structurizr.workspaces";
    public static final String SINGLE_WORKSPACE = "1";

    public static final String AUTO_SAVE_INTERVAL_PROPERTY = "structurizr.autoSaveInterval";
    public static final String DEFAULT_AUTO_SAVE_INTERVAL_IN_MILLISECONDS = "5000";
    public static final String AUTO_REFRESH_INTERVAL_PROPERTY = "structurizr.autoRefreshInterval";
    public static final String DEFAULT_AUTO_REFRESH_INTERVAL_IN_MILLISECONDS = "0";

}