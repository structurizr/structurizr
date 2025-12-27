QUnit.test("Workspace() initialises an empty JSON document", function( assert ) {
    var workspace = new structurizr.Workspace({});

    assert.deepEqual(workspace.documentation, {
        "decisions": [],
        "images": [],
        "sections": []      
    });

    assert.deepEqual(workspace.model, {
        "customElements": [],
        "deploymentNodes": [],
        "people": [],
        "softwareSystems": [],
        "properties": {}
    });

    assert.deepEqual(workspace.views, {
        "componentViews": [],
        "configuration": {
          "branding": {},
          "metadataSymbols": "SquareBrackets",
          "properties": {},
          "styles": {
            "elements": [],
            "relationships": []
          },
          "terminology": {},
          "themes": []
        },
        "containerViews": [],
        "customViews": [],
        "deploymentViews": [],
        "dynamicViews": [],
        "filteredViews": [],
        "systemContextViews": [],
        "systemLandscapeViews": [],
        "imageViews": []
      });
});


QUnit.test("Workspace.id returns -1 when there is no ID", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.id, -1);
});

QUnit.test("Workspace.id returns the ID", function( assert ) {
    var workspace = new structurizr.Workspace({ id: 123 });
    assert.equal(workspace.id, 123);
});

QUnit.test("Workspace.name returns an empty string when there is no name", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.name, "");
});

QUnit.test("Workspace.name returns the name", function( assert ) {
    var workspace = new structurizr.Workspace({ name: "Name" });
    assert.equal(workspace.name, "Name");
});

QUnit.test("Workspace.description returns an empty string when there is no description", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.description, "");
});

QUnit.test("Workspace.description returns the description", function( assert ) {
    var workspace = new structurizr.Workspace({ description: "Description" });
    assert.equal(workspace.description, "Description");
});

QUnit.test("Workspace.getProperty() returns undefined when the property doesn't exist", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.getProperty('structurizr.dslEditor'), undefined);
});

QUnit.test("Workspace.getProperty() returns the named property", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
            properties: {
                'structurizr.dslEditor': 'false'
            }
        }
    );
    assert.equal(workspace.getProperty('structurizr.dslEditor'), 'false');
});

QUnit.test("Workspace.hasElements() returns false when there are no elements in the model", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.hasElements(), false);
});

QUnit.test("Workspace.hasElements() returns true when there are elements in the model", function( assert ) {
    var workspace = new structurizr.Workspace({
        model: {
            people: [
                {
                    id: '1',
                    name: 'User 1'
                }
            ]
        }
    });
    assert.equal(workspace.hasElements(), true);
});

QUnit.test("Workspace.findElementById() returns undefined when the element doesn't exist", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.findElementById('123'), undefined);
});

QUnit.test("Workspace.findElementById() returns the specified element", function( assert ) {
    var workspace = new structurizr.Workspace({
        model: {
            people: [
                {
                    id: '1',
                    name: 'User 1'
                },
                {
                    id: '2',
                    name: 'User 2'
                },
                {
                    id: '3',
                    name: 'User 3'
                }
            ]
        }
    });
    assert.deepEqual(workspace.findElementById('2'), {
        "id": "2",
        "name": "User 2",
        "parentId": undefined,
        "type": "Person",
        "perspectives": [],
        "properties": {}
    });
});

QUnit.test("Workspace.findRelationshipById() returns undefined when the relationship doesn't exist", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.findRelationshipById('123'), undefined);
});

QUnit.test("Workspace.hasViews() returns false when there are no views", function( assert ) {
    var workspace = new structurizr.Workspace({ id: 1 });
    assert.equal(workspace.hasViews(), false);
});

QUnit.test("Workspace.hasViews() returns true when there are views", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                systemContextViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.equal(workspace.hasViews(), true);
});

QUnit.test("Workspace.hasStyles() returns false when there are no styles", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.hasStyles(), false);
});

QUnit.test("Workspace.hasStyles() returns true when there are some element styles", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                configuration: {
                    styles: {
                        elements: [
                            {
                                tag: 'Element'
                            }
                        ],
                        relationships: []
                    }
                }
             } 
        }
    );
    assert.equal(workspace.hasStyles(), true);
});

QUnit.test("Workspace.hasStyles() returns true when there are some relationship styles", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                configuration: {
                    styles: {
                        elements: [],
                        relationships: [
                            {
                                tag: 'Relationship'
                            }
                        ]
                    }
                }
             } 
        }
    );
    assert.equal(workspace.hasStyles(), true);
});

QUnit.test("Workspace.findElementStyleByTag() returns undefined when there is no element style", function( assert ) {
    var workspace = new structurizr.Workspace({});
    assert.equal(workspace.findElementStyleByTag('Element'), undefined);
});

QUnit.test("Workspace.findElementStyleByTag() returns the specified element style", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                configuration: {
                    styles: {
                        elements: [
                            {
                                tag: 'Element',
                                background: '#ffffff'
                            }
                        ],
                        relationships: []
                    }
                }
             } 
        }
    );
    assert.deepEqual(workspace.findElementStyleByTag('Element'),
    {
        "tag": "Element",
        "background": "#ffffff"
    }
    );
});

QUnit.test("Workspace.findViewByKey() returns undefined when the view does not exist", function( assert ) {
    var workspace = new structurizr.Workspace({ id: 1 });
    assert.equal(workspace.findViewByKey('key'), undefined);
});

QUnit.test("Workspace.findViewByKey() returns the specified custom view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                customViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "Custom",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified system landscape view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                systemLandscapeViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "SystemLandscape",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified system context view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                systemContextViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "SystemContext",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified container view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                containerViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "Container",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified component view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                componentViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "Component",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified dynamic view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                dynamicViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "Dynamic",
            "elements": [],
            "relationships": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified deployment view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                deploymentViews: [
                    {
                        key: 'key'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "key": "key",
            "type": "Deployment",
            "environment": "Default",
            "elements": [],
            "relationships": [],
            "animations": [],
            "description": ""
          }
    );
});

QUnit.test("Workspace.findViewByKey() returns the specified filtered view", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                systemLandscapeViews: [
                    {
                        key: 'base'
                    }
                ],
                filteredViews: [
                    {
                        key: 'key',
                        baseViewKey: 'base'
                    }
                ]
             } 
        }
    );
    assert.deepEqual(workspace.findViewByKey('key'),
        {
            "animations": [],
            "baseViewKey": "base",
            "description": "",
            "elements": [],
            "key": "key",
            "relationships": [],
            "type": "Filtered"
        }
    );
});

QUnit.test("Workspace.getPerspectiveNames() returns perspective names from elements and relationships", function( assert ) {
    var workspace = new structurizr.Workspace({
        model: {
            softwareSystems: [
                {
                    id: '1',
                    name: 'A',
                    perspectives: [
                        {
                            name: 'Perspective 1',
                            description: 'Description'
                        }
                    ],
                    relationships: [
                        {
                            id: 3,
                            sourceId: 1,
                            destinationId: 2,
                            perspectives: [
                                {
                                    name: 'Perspective 3',
                                    description: 'Description'
                                }
                            ]
                        }
                    ]
                },
                {
                    id: '2',
                    name: 'B',
                    perspectives: [
                        {
                            name: 'Perspective 2',
                            description: 'Description'
                        }
                    ]
                }
            ]
        }
    });
    assert.deepEqual(workspace.getPerspectiveNames(), ['Perspective 1', 'Perspective 2', 'Perspective 3']);
});

QUnit.test("Workspace() initialises a workspace with sorted element styles", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                configuration: {
                    styles: {
                        elements: [
                            {
                                tag: 'Element',
                                colorScheme: 'Dark',
                                background: '#000000'
                            },
                            {
                                tag: 'Element',
                                colorScheme: 'Light',
                                background: '#ffffff'
                            },
                            {
                                tag: 'Element',
                                background: '#777777'
                            }
                        ],
                        relationships: []
                    }
                }
             } 
        }
    );
    assert.deepEqual(workspace.views.configuration.styles.elements,
        [
            {
                "tag": "Element",
                "background": "#777777"
            },
            {
                "tag": "Element",
                "background": "#000000",
                "colorScheme": "Dark"
            },
            {
                "tag": "Element",
                "background": "#ffffff",
                "colorScheme": "Light"
            }
        ]     
    );
});

QUnit.test("Workspace() initialises a workspace with sorted relationship styles", function( assert ) {
    var workspace = new structurizr.Workspace(
        {
             views: {
                configuration: {
                    styles: {
                        relationships: [
                            {
                                tag: 'Relationship',
                                colorScheme: 'Dark',
                                color: '#ffffff'
                            },
                            {
                                tag: 'Relationship',
                                colorScheme: 'Light',
                                color: '#000000'
                            },
                            {
                                tag: 'Relationship',
                                color: '#777777'
                            }
                        ]
                    }
                }
             } 
        }
    );
    assert.deepEqual(workspace.views.configuration.styles.relationships,
        [
            {
                "tag": "Relationship",
                "color": "#777777"
            },
            {
                "tag": "Relationship",
                "color": "#ffffff",
                "colorScheme": "Dark"
            },
            {
                "tag": "Relationship",
                "color": "#000000",
                "colorScheme": "Light"
            }
        ]    
    );
});

