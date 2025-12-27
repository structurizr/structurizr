// findElementStyle

QUnit.test("structurizr.ui.findElementStyle() finds the element style", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "tags": "Element,Software System"
                }
            ]
        },
        "views": {
            "configuration": {
                "styles": {
                    "elements": [
                        {
                            "tag": "Element",
                            "shape": "RoundedBox"
                        }
                    ]
                }
            }
        }
    });

    structurizr.ui.themes = [];

    var element = structurizr.workspace.findElementById('1');
    var elementStyle = structurizr.ui.findElementStyle(element, false);

    assert.equal(elementStyle.width, 450);
    assert.equal(elementStyle.height, 300);
    assert.equal(elementStyle.background, '#ffffff');
    assert.equal(elementStyle.color, '#444444');
    assert.equal(elementStyle.fontSize, 24);
    assert.equal(elementStyle.shape, 'RoundedBox');
    assert.equal(elementStyle.icon, undefined);
    assert.equal(elementStyle.border, 'Solid');
    assert.equal(elementStyle.stroke, '#444444');
    assert.equal(elementStyle.opacity, 100);
    assert.equal(elementStyle.metadata, true);
    assert.equal(elementStyle.description, true);
    assert.deepEqual(elementStyle.tags, ['Element']);
});

QUnit.test("structurizr.ui.findElementStyle() finds the element style when there is a theme", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "tags": "Element,Software System"
                }
            ]
        },
        "views": {
            "configuration": {
                "styles": {
                    "elements": [
                        {
                            "tag": "Element",
                            "shape": "RoundedBox"
                        },
                        {
                            "tag": "Person",
                            "shape": "Person"
                        }
                    ]
                }
            }
        }
    });

    structurizr.ui.themes =
    [
        {
            elements:
            [
                {
                    tag: 'Element',
                    shape: 'Box',
                    background: '#ff0000'
                }
            ]
        }
    ];

    var element = structurizr.workspace.findElementById('1');
    var elementStyle = structurizr.ui.findElementStyle(element);

    assert.equal(elementStyle.width, 450);
    assert.equal(elementStyle.height, 300);
    assert.equal(elementStyle.background, '#ff0000'); // set in theme
    assert.equal(elementStyle.color, '#444444');
    assert.equal(elementStyle.fontSize, 24);
    assert.equal(elementStyle.shape, 'RoundedBox'); // set in workspace
    assert.equal(elementStyle.icon, undefined);
    assert.equal(elementStyle.border, 'Solid');
    assert.equal(elementStyle.stroke, '#e60000');
    assert.equal(elementStyle.opacity, 100);
    assert.equal(elementStyle.metadata, true);
    assert.equal(elementStyle.description, true);
    assert.deepEqual(elementStyle.tags, ['Element']);
});

// findRelationshipStyle

QUnit.test("structurizr.ui.findRelationshipStyle() finds the relationship style", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "name": "Name 1",
                    "description": "Description",
                    "tags": "Element,Software System",
                    "relationships": [
                        {
                            "id": "3",
                            "sourceId": "1",
                            "description": "Uses",
                            "technology": "",
                            "destinationId": "2",
                            "tags": "Relationship"
                        }
                    ]
                },
                {
                    "id": "2",
                    "name": "Name 2",
                    "description": "Description",
                    "tags": "Element,Software System"
                }
            ]
        },
        "views": {
            "configuration": {
                "styles": {
                    "relationships": [
                        {
                            "tag": "Relationship",
                            "color": "#ff0000"
                        }
                    ]
                }
            }
        }
    });

    structurizr.ui.themes = [];

    var relationship = structurizr.workspace.findRelationshipById('3');
    var relationshipStyle = structurizr.ui.findRelationshipStyle(relationship);

    assert.equal(relationshipStyle.thickness, 2);
    assert.equal(relationshipStyle.color, '#ff0000');
    assert.equal(relationshipStyle.dashed, true);
    assert.equal(relationshipStyle.routing, 'Direct');
    assert.equal(relationshipStyle.fontSize, 24);
    assert.equal(relationshipStyle.width, 200);
    assert.equal(relationshipStyle.position, 50);
    assert.equal(relationshipStyle.opacity, 100);
    assert.deepEqual(relationshipStyle.tags, ['Relationship']);
});

QUnit.test("structurizr.ui.findRelationshipStyle() finds the relationship style when there is a theme", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "name": "Name 1",
                    "description": "Description",
                    "tags": "Element,Software System",
                    "relationships": [
                        {
                            "id": "3",
                            "sourceId": "1",
                            "description": "Uses",
                            "technology": "",
                            "destinationId": "2",
                            "tags": "Relationship"
                        }
                    ]
                },
                {
                    "id": "2",
                    "name": "Name 2",
                    "description": "Description",
                    "tags": "Element,Software System"
                }
            ]
        },
        "views": {
            "configuration": {
                "styles": {
                    "relationships": [
                        {
                            "tag": "Relationship",
                            "color": "#ff0000"
                        },
                        {
                            "tag": "Async",
                            "style": "dashed"
                        }
                    ]
                }
            }
        }
    });

    structurizr.ui.themes = [
        {
            relationships:
            [
                {
                    tag: 'Relationship',
                    color: '#000000',
                    thickness: 5
                }
            ]
        }
    ];

    var relationship = structurizr.workspace.findRelationshipById('3');
    var relationshipStyle = structurizr.ui.findRelationshipStyle(relationship);

    assert.equal(relationshipStyle.thickness, 5); // set in theme
    assert.equal(relationshipStyle.color, '#ff0000'); // set in workspace
    assert.equal(relationshipStyle.dashed, true);
    assert.equal(relationshipStyle.routing, 'Direct');
    assert.equal(relationshipStyle.fontSize, 24);
    assert.equal(relationshipStyle.width, 200);
    assert.equal(relationshipStyle.position, 50);
    assert.equal(relationshipStyle.opacity, 100);
    assert.deepEqual(relationshipStyle.tags, ['Relationship']);
});

QUnit.test("getTitleForView() returns the view's title", function( assert ) {
    const view = {
        title: "Title"
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'Title');
});

QUnit.test("getTitleForView() returns the computed name of a system landscape view (no enterprise name)", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
        }
    });

    const view = {
        type: 'SystemLandscape'
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'System Landscape View');
});

QUnit.test("getTitleForView() returns the computed name of a system landscape view (no enterprise name)", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            enterprise: {
                name: "Enterprise"
            }
        }
    });

    const view = {
        type: 'SystemLandscape'
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'System Landscape View: Enterprise');
});

QUnit.test("getTitleForView() returns the computed name of a system context view", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "name": "Software System 1"
                }
            ]
        }
    });

    const view = {
        type: 'SystemContext',
        softwareSystemId: '1'
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'System Context View: Software System 1');
});

QUnit.test("getTitleForView() returns the computed name of a container view", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "name": "Software System 1"
                }
            ]
        }
    });

    const view = {
        type: 'Container',
        softwareSystemId: '1'
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'Container View: Software System 1');
});

QUnit.test("getTitleForView() returns the computed name of a component view", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        "model": {
            "softwareSystems": [
                {
                    "id": "1",
                    "name": "Software System 1",
                    "containers": [
                        {
                            id: "2",
                            name: "Container 1"
                        }
                    ]
                    
                }
            ]
        }
    });

    const view = {
        type: 'Component',
        containerId: '2'
    };
    assert.equal(structurizr.ui.getTitleForView(view), 'Component View: Software System 1 - Container 1');
});

QUnit.test("structurizr.ui.getBranding() returns the default branding when no branding has been defined", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({});
    assert.deepEqual(structurizr.ui.getBranding(),
        {
            "font": {
                "name": "Arial",
                "url": undefined
            }
        }
    );
});

QUnit.test("structurizr.ui.getBranding() returns the workspace branding when specified", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({
        views: {
            configuration: {
                branding: {
                    logo: 'workspace-logo',
                    font: {
                        name: 'workspace-name',
                        url: 'workspace-url'
                    }
                }
            }
        }
    });
    structurizr.ui.themes =
        [
            {
                elements: [],
                relationships: [],
                logo: 'theme-logo',
                font: {
                    name: 'theme-name',
                    url: 'theme-url'
                }
            }
        ];
    assert.deepEqual(structurizr.ui.getBranding(),
        {
            "logo": "workspace-logo",
            "font": {
                "name": "workspace-name",
                "url": "workspace-url"
            }
        }
    );
});

QUnit.test("structurizr.ui.getBranding() returns the theme branding when no workspace branding has been defined", function( assert ) {
    structurizr.workspace = new structurizr.Workspace({});
    structurizr.ui.themes =
        [
            {
                elements: [],
                relationships: [],
                logo: 'theme-logo',
                font: {
                    name: 'theme-name',
                    url: 'theme-url'
                }
            }
        ];
    assert.deepEqual(structurizr.ui.getBranding(),
        {
            "logo": "theme-logo",
            "font": {
                "name": "theme-name",
                "url": "theme-url"
            }
        }
    );
});

