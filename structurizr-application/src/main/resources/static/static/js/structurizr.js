var structurizr = structurizr || {

    io: {},
    ui: {},
    util: {},

    constants: {
        CUSTOM_ELEMENT_TYPE: "Custom",
        PERSON_ELEMENT_TYPE: "Person",
        SOFTWARE_SYSTEM_ELEMENT_TYPE: "SoftwareSystem",
        CONTAINER_ELEMENT_TYPE: "Container",
        COMPONENT_ELEMENT_TYPE: "Component",
        DEPLOYMENT_NODE_ELEMENT_TYPE: "DeploymentNode",
        INFRASTRUCTURE_NODE_ELEMENT_TYPE: "InfrastructureNode",
        SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE: "SoftwareSystemInstance",
        CONTAINER_INSTANCE_ELEMENT_TYPE: "ContainerInstance",
        BOUNDARY_ELEMENT_TYPE: "Boundary",
        GROUP_ELEMENT_TYPE: "Group",

        CUSTOM_VIEW_TYPE: "Custom",
        SYSTEM_LANDSCAPE_VIEW_TYPE: "SystemLandscape",
        SYSTEM_CONTEXT_VIEW_TYPE: "SystemContext",
        CONTAINER_VIEW_TYPE: "Container",
        COMPONENT_VIEW_TYPE: "Component",
        DYNAMIC_VIEW_TYPE: "Dynamic",
        DEPLOYMENT_VIEW_TYPE: "Deployment",
        FILTERED_VIEW_TYPE: "Filtered",
        IMAGE_VIEW_TYPE: "Image",

        DEFAULT_DEPLOYMENT_ENVIRONMENT_NAME: "Default",

        CONTENT_TYPE_IMAGE_SVG: 'image/svg+xml',
        CONTENT_TYPE_IMAGE_PNG: 'image/png',
        CONTENT_TYPE_IMAGE_JPG: 'image/jpeg',

        INTRA_WORKSPACE_URL_PREFIX: '{workspace}',
        INTER_WORKSPACE_URL_PREFIX: '{workspace:',
        INTER_WORKSPACE_URL_SEPARATOR: ':',
        INTER_WORKSPACE_URL_SUFFIX: '}',

        DEFAULT_TAGS: [
            'Element',
            'Relationship',
            'Person',
            'Software System',
            'Container',
            'Component',
            'Deployment Node',
            'Infrastructure Node',
            'Software System Instance',
            'Container Instance'
        ]
    }
};