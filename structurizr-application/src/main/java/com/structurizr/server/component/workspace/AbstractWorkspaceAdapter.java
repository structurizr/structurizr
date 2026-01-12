package com.structurizr.server.component.workspace;

abstract class AbstractWorkspaceAdapter implements WorkspaceAdapter {

    static final String WORKSPACE_PROPERTIES_FILENAME = "workspace.properties";

    static final String WORKSPACE_DSL_FILENAME = "workspace.dsl";

    static final String WORKSPACE_JSON_FILENAME = "workspace.json";
    static final String WORKSPACE_VERSION_JSON_FILENAME = "workspace-%s.json";
    static final String VERSION_TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    static final String WORKSPACE_VERSION_JSON_FILENAME_REGEX = "workspace-\\d{17}\\.json";

    static final String BRANCHES_DIRECTORY_NAME = "branches";
    static final String IMAGES_DIRECTORY_NAME = "images";

}