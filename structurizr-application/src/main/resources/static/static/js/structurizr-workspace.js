// This is a read-only wrapper around a Structurizr workspace, providing some functions to assist with finding model elements and views.
structurizr.Workspace = class Workspace {

    #json;

    id;
    name;
    description;
    properties;
    lastModifiedDate;
    model;
    documentation;
    views;

    #workspace;
    #elementsById = {};
    #relationshipsById = {};
    #allViews = [];
    #views = []

    constructor(json) {
        this.#json = json;
        this.#workspace = JSON.parse(JSON.stringify(json));

        this.#initWorkspace();
        this.id = this.#workspace.id;
        this.name = this.#workspace.name;
        this.description = this.#workspace.description;
        this.lastModifiedDate = this.#workspace.lastModifiedDate;
        this.version = this.#workspace.version;

        this.#initDocumentation(this.#workspace);
        this.documentation = this.#workspace.documentation;

        this.#initModel();
        this.model = this.#workspace.model;

        this.#initViews();
        this.views = this.#workspace.views;
    }

    getJson() {
        return JSON.parse(JSON.stringify(this.#json));
    }

    getProperty(name) {
        return this.#workspace.properties[name];
    }

    getScope(name) {
        return this.#workspace.configuration.scope;
    }

    getModelProperty(name) {
        return this.#workspace.model.properties[name];
    }

    getViewSetProperty(name) {
        return this.#workspace.views.configuration[name];
    }

    getViewOrViewSetProperty(viewKey, name) {
        return this.#workspace.views.configuration[name];
    }

    #initWorkspace() {
        if (this.#workspace.id === undefined) {
            this.#workspace.id = -1;
        }

        if (this.#workspace.name === undefined) {
            this.#workspace.name = '';
        }

        if (this.#workspace.description === undefined) {
            this.#workspace.description = '';
        }

        if (this.#workspace.properties === undefined) {
            this.#workspace.properties = {};
        }

        if (this.#workspace.configuration === undefined) {
            this.#workspace.configuration = {};
        }

        if (this.#workspace.lastModifiedDate === undefined) {
            this.#workspace.lastModifiedDate = new Date().toISOString();
        }
    }

    #initDocumentation(workspaceOrElement) {
        if (workspaceOrElement.documentation === undefined) {
            workspaceOrElement.documentation = {};
        }

        if (workspaceOrElement.documentation.sections === undefined) {
            workspaceOrElement.documentation.sections = [];
        }

        if (workspaceOrElement.documentation.decisions === undefined) {
            workspaceOrElement.documentation.decisions = [];
        }

        if (workspaceOrElement.documentation.images === undefined) {
            workspaceOrElement.documentation.images = [];
        }
    }

    hasDocumentation() {
        // check for workspace level sections
        if (this.#workspace.documentation.sections.length > 0) {
            return true;
        }

        var result = false;
        this.#workspace.model.softwareSystems.forEach(function(softwareSystem) {
            if (softwareSystem.documentation.sections.length > 0) {
                result = true;
            }

            if (result === false) {
                if (softwareSystem.containers) {
                    softwareSystem.containers.forEach(function (container) {
                        if (container.documentation.sections.length > 0) {
                            result = true;
                        }

                        if (result === false) {
                            if (container.components) {
                                container.components.forEach(function (component) {
                                    if (component.documentation.sections.length > 0) {
                                        result = true;
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        return result;
    }

    hasDecisions() {
        // check for workspace level decisions
        if (this.#workspace.documentation.decisions.length > 0) {
            return true;
        }

        var result = false;
        this.#workspace.model.softwareSystems.forEach(function(softwareSystem) {
            if (softwareSystem.documentation.decisions.length > 0) {
                result = true;
            }

            if (result === false) {
                if (softwareSystem.containers) {
                    softwareSystem.containers.forEach(function (container) {
                        if (container.documentation.decisions.length > 0) {
                            result = true;
                        }

                        if (result === false) {
                            if (container.components) {
                                container.components.forEach(function (component) {
                                    if (component.documentation.decisions.length > 0) {
                                        result = true;
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        return result;
    }

    #initModel() {
        if (this.#workspace.model === undefined) {
            this.#workspace.model = {};
        }

        if (this.#workspace.model.properties === undefined) {
            this.#workspace.model.properties = {};
        }

        var model = this.#workspace.model;

        if (model.customElements) {
            this.#sortArrayByNameAscending(model.customElements);
            for (i = 0; i < model.customElements.length; i++) {
                var customElement = model.customElements[i];
                customElement.parentId = undefined;
                this.#registerElement(customElement, structurizr.constants.CUSTOM_ELEMENT_TYPE);
            }
        } else {
            model.customElements = [];
        }

        if (model.people) {
            this.#sortArrayByNameAscending(model.people);
            for (i = 0; i < model.people.length; i++) {
                var person = model.people[i];
                person.parentId = undefined;
                this.#registerElement(person, structurizr.constants.PERSON_ELEMENT_TYPE);
            }
        } else {
            model.people = [];
        }

        if (model.softwareSystems) {
            this.#sortArrayByNameAscending(model.softwareSystems);
            for (i = 0; i < model.softwareSystems.length; i++) {
                var softwareSystem = model.softwareSystems[i];
                softwareSystem.parentId = undefined;
                this.#registerElement(softwareSystem, structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE);

                if (softwareSystem.containers) {
                    this.#sortArrayByNameAscending(softwareSystem.containers);
                    for (var j = 0; j < softwareSystem.containers.length; j++) {
                        var container = softwareSystem.containers[j];
                        container.parentId = softwareSystem.id;
                        this.#registerElement(container, structurizr.constants.CONTAINER_ELEMENT_TYPE);

                        if (container.components) {
                            this.#sortArrayByNameAscending(container.components);
                            for (var k = 0; k < container.components.length; k++) {
                                var component = container.components[k];
                                component.parentId = container.id;
                                this.#registerElement(component, structurizr.constants.COMPONENT_ELEMENT_TYPE);


                                this.#initDocumentation(component);
                            }
                        }

                        this.#initDocumentation(container);
                    }
                }

                this.#initDocumentation(softwareSystem);
            }
        } else {
            model.softwareSystems = [];
        }

        if (model.deploymentNodes) {
            this.#sortArrayByNameAscending(model.deploymentNodes);
            for (var i = 0; i < model.deploymentNodes.length; i++) {
                const deploymentNode = model.deploymentNodes[i];
                this.#registerDeploymentNode(deploymentNode, undefined);
            }
        } else {
            model.deploymentNodes = [];
        }
    }

    #registerDeploymentNode(deploymentNode, parent) {
        if (parent === undefined) {
            deploymentNode.parentId = undefined;
        } else {
            deploymentNode.parentId = parent.id;
        }

        if (deploymentNode.environment === undefined) {
            deploymentNode.environment = structurizr.constants.DEFAULT_DEPLOYMENT_ENVIRONMENT_NAME;
        }

        this.#registerElement(deploymentNode, structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE);

        if (deploymentNode.children) {
            this.#sortArrayByNameAscending(deploymentNode.children);
            for (var i = 0; i < deploymentNode.children.length; i++) {
                const child = deploymentNode.children[i];
                this.#registerDeploymentNode(child, deploymentNode);
            }
        } else {
            deploymentNode.children = [];
        }

        if (deploymentNode.softwareSystemInstances) {
            for (var i = 0; i < deploymentNode.softwareSystemInstances.length; i++) {
                const softwareSystemInstance = deploymentNode.softwareSystemInstances[i];
                const softwareSystem = this.findElementById(softwareSystemInstance.softwareSystemId);
                softwareSystemInstance.name = softwareSystem.name;
                if (softwareSystemInstance.description === undefined) {
                    softwareSystemInstance.description = softwareSystem.description;
                }
                if (softwareSystemInstance.url === undefined) {
                    softwareSystemInstance.url = softwareSystem.url;
                }
                softwareSystemInstance.parentId = deploymentNode.id;

                if (softwareSystemInstance.environment === undefined) {
                    softwareSystemInstance.environment = deploymentNode.environment;
                }

                this.#registerElement(softwareSystemInstance, structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE);
            }
        } else {
            deploymentNode.softwareSystemInstances = [];
        }

        if (deploymentNode.containerInstances) {
            for (var i = 0; i < deploymentNode.containerInstances.length; i++) {
                const containerInstance = deploymentNode.containerInstances[i];
                const container = this.findElementById(containerInstance.containerId);
                containerInstance.name = container.name;
                if (containerInstance.description === undefined) {
                    containerInstance.description = container.description;
                }
                containerInstance.technology = container.technology;
                if (containerInstance.url === undefined) {
                    containerInstance.url = container.url;
                }
                containerInstance.parentId = deploymentNode.id;

                if (containerInstance.environment === undefined) {
                    containerInstance.environment = deploymentNode.environment;
                }

                this.#registerElement(containerInstance, structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE);
            }
        } else {
            deploymentNode.containerInstances = [];
        }

        if (deploymentNode.infrastructureNodes) {
            for (var i = 0; i < deploymentNode.infrastructureNodes.length; i++) {
                const infrastructureNode = deploymentNode.infrastructureNodes[i];
                infrastructureNode.parentId = deploymentNode.id;
                if (infrastructureNode.environment === undefined) {
                    infrastructureNode.environment = deploymentNode.environment;
                }
                this.#registerElement(infrastructureNode, structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE);
            }
        } else {
            deploymentNode.infrastructureNodes = [];
        }
    }

    #registerElement(element, type) {
        if (element) {
            element.type = type;

            this.#elementsById[element.id] = element;

            if (element.relationships) {
                for (var i = 0; i < element.relationships.length; i++) {
                    this.#registerRelationship(element.relationships[i]);
                }
            }

            if (element.url && element.url.trim().length === 0) {
                element.url = undefined;
            }

            if (element.properties === undefined) {
                element.properties = {};
            } else {
                this.#sortProperties(element);
            }

            if (element.perspectives === undefined) {
                element.perspectives = [];
            }
        }
    }

    hasElements() {
        return Object.keys(this.#elementsById).length > 0;
    }

    getElements() {
        return Object.values(this.#elementsById);
    }

    findElementById(id) {
        return this.#elementsById[id];
    }

    getTags() {
        const self = this;
        const tags = [];

        Object.keys(this.#elementsById).forEach(function(id) {
            const element = self.#elementsById[id];
            if (element.tags) {
                element.tags.split(',').forEach(function (tag) {
                    tag = structurizr.util.trim(tag);

                    if (tag.length > 0 && tags.indexOf(tag) === -1) {
                        tags.push(tag);
                    }
                });
            }
        });

        Object.keys(this.#relationshipsById).forEach(function(id) {
            const relationship = self.#relationshipsById[id];
            if (relationship.tags) {
                relationship.tags.split(',').forEach(function (tag) {
                    tag = structurizr.util.trim(tag);

                    if (tag.length > 0 && tags.indexOf(tag) === -1) {
                        tags.push(tag);
                    }
                });
            }
        });

        tags.sort();
        return tags;
    };

    getUserDefinedTags() {
        if (this.views.configuration && this.views.configuration.properties) {
            const tagsAsString = this.views.configuration.properties['structurizr.filter.tags'];
            if (tagsAsString) {
                return tagsAsString.split(',');
            }
        }

        const tags = this.getTags();

        structurizr.constants.DEFAULT_TAGS.forEach(function(tag) {
            const index = tags.indexOf(tag);
            if (index > -1) {
                tags.splice(index, 1);
            }
        });

        return tags;
    }

    getAllTagsForElement(element) {
        var tags = (element.tags ? element.tags : '');

        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE) {
            // we also need to prepend the set of tags of the software system
            const softwareSystem = this.findElementById(element.softwareSystemId);
            if (softwareSystem && softwareSystem.tags) {
                tags = (softwareSystem.tags + ',' + tags);
            }
        } else if (element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE) {
            // we also need to prepend the set of tags of the container
            const container = this.findElementById(element.containerId);
            if (container && container.tags) {
                tags = (container.tags + ',' + tags);
            }
        }

        return tags.split(',').filter(function(tag) {
            return tag !== undefined && tag.length > 0;
        });
    }

    getAllPropertiesForElement(element) {
        var properties = {};

        if (element.properties) {
            properties = element.properties;
        }

        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE) {
            // we also need to add the properties from the base element
            var softwareSystem = this.findElementById(element.softwareSystemId);
            if (softwareSystem && softwareSystem.properties) {
                Object.keys(softwareSystem.properties).forEach(function(key) {
                    if (!properties[key]) {
                        properties[key] = softwareSystem.properties[key];
                    }
                });
            }
        } else if (element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE) {
            // we also need to prepend the set of properties of the container
            var container = this.findElementById(element.containerId);
            if (container && container.properties) {
                Object.keys(container.properties).forEach(function(key) {
                    if (!properties[key]) {
                        properties[key] = container.properties[key];
                    }
                });
            }
        }

        return properties;
    }

    getAllTagsForRelationship(relationship) {
        var tags = '';
        if (relationship.tags) {
            tags = relationship.tags;
        }

        var linkedRelationshipId = relationship.linkedRelationshipId;
        while (linkedRelationshipId !== undefined) {
            // we also need to prepend the set of tags of the linked relationship
            var linkedRelationship = this.findRelationshipById(linkedRelationshipId);
            if (linkedRelationship && linkedRelationship.tags) {
                tags = linkedRelationship.tags + ',' + tags;
            }

            linkedRelationshipId = linkedRelationship.linkedRelationshipId;
        }

        return tags.split(',').filter(function(tag) {
            return tag !== undefined && tag.length > 0;
        });
    }

    getAllPropertiesForRelationship(relationship) {
        var properties = {};

        if (relationship.properties) {
            properties = relationship.properties;
        }

        // we also need to add the properties from the linked relationship(s)
        var linkedRelationshipId = relationship.linkedRelationshipId;
        while (linkedRelationshipId !== undefined) {
            var linkedRelationship = this.findRelationshipById(linkedRelationshipId);
            if (linkedRelationship && linkedRelationship.properties) {
                Object.keys(linkedRelationship.properties).forEach(function(key) {
                    if (!properties[key]) {
                        properties[key] = linkedRelationship.properties[key];
                    }
                });
            }

            linkedRelationshipId = linkedRelationship.linkedRelationshipId;
        }

        return properties;
    }

    #registerRelationship(relationship) {
        this.#relationshipsById[relationship.id] = relationship;

        // use the URL from the linked relationship(s) if necessary
        var linkedRelationshipId = relationship.linkedRelationshipId;
        while ((relationship.url === undefined || relationship.url.trim().length === 0) && linkedRelationshipId !== undefined) {
            var linkedRelationship = this.findRelationshipById(linkedRelationshipId);
            if (linkedRelationship) {
                relationship.url = linkedRelationship.url;
                linkedRelationshipId = linkedRelationship.linkedRelationshipId;
            } else {
                linkedRelationshipId = undefined;
            }
        }

        if (relationship.url && relationship.url.trim().length === 0) {
            relationship.url = undefined;
        }

        if (relationship.properties === undefined) {
            relationship.properties = {};
        } else {
            this.#sortProperties(relationship);
        }

        if (relationship.perspectives === undefined) {
            relationship.perspectives = [];
        }
    }

    getRelationships() {
        return Object.values(this.#relationshipsById);
    }

    findRelationshipById(id) {
        return this.#relationshipsById[id];
    }

    getPerspectiveNames() {
        const self = this;
        const names = [];

        Object.keys(this.#elementsById).forEach(function(id) {
            const element = self.#elementsById[id];
            element.perspectives.forEach(function(perspective) {
                const name = structurizr.util.trim(perspective.name);
                if (names.indexOf(name) === -1) {
                    names.push(name);
                }
            });
        });

        Object.keys(this.#relationshipsById).forEach(function(id) {
            const relationship = self.#relationshipsById[id];
            relationship.perspectives.forEach(function(perspective) {
                const name = structurizr.util.trim(perspective.name);
                if (names.indexOf(name) === -1) {
                    names.push(name);
                }
            });
        });

        names.sort();
        return names;
    }

    #initViews() {
        const self = this;

        if (this.#workspace.views === undefined) {
            this.#workspace.views = {};
        }

        if (this.#workspace.views.customViews === undefined) {
            this.#workspace.views.customViews = [];
        }

        this.#workspace.views.customViews.forEach(function(view) {
            view.type = structurizr.constants.CUSTOM_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.systemLandscapeViews === undefined) {
            this.#workspace.views.systemLandscapeViews = [];
        }

        this.#workspace.views.systemLandscapeViews.forEach(function(view) {
            view.type = structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.systemContextViews === undefined) {
            this.#workspace.views.systemContextViews = [];
        }

        this.#workspace.views.systemContextViews.forEach(function(view) {
            view.type = structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.containerViews === undefined) {
            this.#workspace.views.containerViews = [];
        }

        this.#workspace.views.containerViews.forEach(function(view) {
            view.type = structurizr.constants.CONTAINER_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.componentViews === undefined) {
            this.#workspace.views.componentViews = [];
        }

        this.#workspace.views.componentViews.forEach(function(view) {
            view.type = structurizr.constants.COMPONENT_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.dynamicViews === undefined) {
            this.#workspace.views.dynamicViews = [];
        }

        this.#workspace.views.dynamicViews.forEach(function(view) {
            view.type = structurizr.constants.DYNAMIC_VIEW_TYPE;
            self.#registerView(view);

            // and sort the relationships
            view.relationships.sort(function(a, b) {
                if (a.order === b.order) {
                    return a.id - b.id;
                } else {
                    return a.order - b.order
                }
            });
        });

        if (this.#workspace.views.deploymentViews === undefined) {
            this.#workspace.views.deploymentViews = [];
        }

        this.#workspace.views.deploymentViews.forEach(function(view) {
            if (view.environment === undefined || view.environment.trim().length === 0) {
                view.environment = structurizr.constants.DEFAULT_DEPLOYMENT_ENVIRONMENT_NAME;
            }

            view.type = structurizr.constants.DEPLOYMENT_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.filteredViews === undefined) {
            this.#workspace.views.filteredViews = [];
        }

        this.#workspace.views.filteredViews.forEach(function(view) {
            view.type = structurizr.constants.FILTERED_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.imageViews === undefined) {
            this.#workspace.views.imageViews = [];
        }

        this.#workspace.views.imageViews.forEach(function(view) {
            view.type = structurizr.constants.IMAGE_VIEW_TYPE;
            self.#registerView(view);
        });

        if (this.#workspace.views.configuration === undefined) {
            this.#workspace.views.configuration = {};
        }

        if (this.#workspace.views.configuration.properties === undefined) {
            this.#workspace.views.configuration.properties = {};
        }

        if (this.#workspace.views.configuration.styles === undefined) {
            this.#workspace.views.configuration.styles = {};
        }

        if (this.#workspace.views.configuration.styles.elements === undefined) {
            this.#workspace.views.configuration.styles.elements = [];
        } else {
            // sort element styles by color scheme (unspecified first, followed by dark and light) and tag
            this.#workspace.views.configuration.styles.elements.sort(structurizr.util.sortStyles);
        }

        if (this.#workspace.views.configuration.styles.relationships === undefined) {
            this.#workspace.views.configuration.styles.relationships = [];
        } else {
            // sort relationship styles by color scheme (unspecified first, followed by dark and light) and tag
            this.#workspace.views.configuration.styles.relationships.sort(structurizr.util.sortStyles);
        }

        if (this.#workspace.views.configuration.metadataSymbols === undefined) {
            this.#workspace.views.configuration.metadataSymbols = 'SquareBrackets';
        }

        if (this.#workspace.views.configuration.branding === undefined) {
            this.#workspace.views.configuration.branding = {};
        }

        if (this.#workspace.views.configuration.terminology === undefined) {
            this.#workspace.views.configuration.terminology = {};
        }

        if (this.#workspace.views.configuration.themes === undefined) {
            this.#workspace.views.configuration.themes = [];
        }

        this.#sortViews();
    }

    #registerView(view) {
        this.#allViews.push(view);

        if (view.description === undefined) {
            view.description = '';
        }

        if (view.type !== structurizr.constants.IMAGE_VIEW_TYPE) {
            if (view.elements === undefined) {
                view.elements = [];
            }

            if (view.relationships === undefined) {
                view.relationships = [];
            }

            if (view.type !== structurizr.constants.DYNAMIC_VIEW_TYPE) {
                if (view.animations === undefined) {
                    view.animations = [];
                }

                view.animations.forEach(function (animationStep) {
                    if (animationStep.elements === undefined) {
                        animationStep.elements = [];
                    }
                    if (animationStep.relationships === undefined) {
                        animationStep.relationships = [];
                    }
                });
            }
        }
    }

    hasViews() {
        return this.#views.length > 0;
    }

    getViews() {
        return this.#views;
    }

    findSystemContextViewsForSoftwareSystem(softwareSystemId) {
        const views = [];

        for (var i = 0; i < this.#views.length; i++) {
            var view = this.#views[i];

            if (view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE && view.softwareSystemId === softwareSystemId) {
                views.push(view);
            } else if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                var baseView = this.findViewByKey(view.baseViewKey);
                if (baseView.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE && baseView.softwareSystemId === softwareSystemId) {
                    views.push(view);
                }
            }
        }

        return views;
    };

    findContainerViewsForSoftwareSystem(softwareSystemId) {
        const views = [];

        for (var i = 0; i < this.#views.length; i++) {
            var view = this.#views[i];

            if (view.type === structurizr.constants.CONTAINER_VIEW_TYPE && view.softwareSystemId === softwareSystemId) {
                views.push(view);
            } else if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                var baseView = this.findViewByKey(view.baseViewKey);
                if (baseView.type === structurizr.constants.CONTAINER_VIEW_TYPE && baseView.softwareSystemId === softwareSystemId) {
                    views.push(view);
                }
            }
        }

        return views;
    };

    findComponentViewsForContainer(containerId) {
        const views = [];

        for (var i = 0; i < this.#views.length; i++) {
            var view = this.#views[i];

            if (view.type === structurizr.constants.COMPONENT_VIEW_TYPE && view.containerId === containerId) {
                views.push(view);
            } else if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                var baseView = this.findViewByKey(view.baseViewKey);
                if (baseView.type === structurizr.constants.COMPONENT_VIEW_TYPE && baseView.containerId === containerId) {
                    views.push(view);
                }
            }
        }

        return views;
    };

    findDynamicViewsForElement(elementId) {
        const views = [];

        for (var i = 0; i < this.views.dynamicViews.length; i++) {
            var view = this.views.dynamicViews[i];

            if (view.elementId === elementId) {
                views.push(view);
            }
        }

        return views;
    };

    findImageViewsForElement(elementId) {
        const views = [];

        for (var i = 0; i < this.views.imageViews.length; i++) {
            var view = this.views.imageViews[i];

            if (view.elementId === elementId) {
                views.push(view);
            }
        }

        return views;
    };

    #sortViews() {
        var listOfViews = [];
        var filters = [];

        var viewTypeOrders = [ 'SystemLandscape', 'SystemContext', 'Container', 'Component', 'Code', 'Dynamic', 'Deployment', 'Image' ];
        var elementTypeOrders = [ '*', 'SoftwareSystem', 'Container', 'Component' ];

        this.#allViews.forEach(function(view) {
            if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                filters.push(view.baseViewKey);
            }
        });

        for (var i = 0; i < this.#allViews.length; i++) {
            const view = this.#allViews[i];

            if (filters.indexOf(view.key) === -1) {
                var obj = {
                    view: view,
                    key: view.key,
                };

                if (view.type === 'Filtered') {
                    var baseView = this.findViewByKey(view.baseViewKey);

                    obj.viewTypeOrder = viewTypeOrders.indexOf(baseView.type);
                    obj.element = this.#findElementForView(baseView);
                } else {
                    obj.viewTypeOrder = viewTypeOrders.indexOf(view.type);
                    obj.element = this.#findElementForView(view);
                }

                if (obj.element) {
                    obj.scope = obj.element.canonicalName;

                    if (obj.element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE) {
                        obj.elementTypeOrder = elementTypeOrders.indexOf(structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE);
                        obj.softwareSystem = obj.element;
                    } else if (obj.element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
                        obj.elementTypeOrder = elementTypeOrders.indexOf(structurizr.constants.CONTAINER_ELEMENT_TYPE);
                        obj.softwareSystem = this.findElementById(obj.element.parentId);
                    } else if (obj.element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE) {
                        obj.elementTypeOrder = elementTypeOrders.indexOf(structurizr.constants.COMPONENT_ELEMENT_TYPE);
                        obj.viewTypeOrder = viewTypeOrders.indexOf("Code");
                        const container = this.findElementById(obj.element.parentId);
                        obj.softwareSystem = this.findElementById(container.parentId);
                    }
                } else {
                    obj.elementTypeOrder = elementTypeOrders.indexOf('*');
                    obj.scope = '/';
                }

                listOfViews.push(obj);
            }
        }

        try {
            if (this.#workspace.views.configuration.viewSortOrder === 'Key' || (this.#workspace.views.configuration.properties && this.#workspace.views.configuration.properties['structurizr.sort'] === 'key')) {
                listOfViews.sort(function (a, b) {
                    return a.key.localeCompare(b.key);
                });
            } else if (this.#workspace.views.configuration.viewSortOrder === 'Type' || (this.#workspace.views.configuration.properties && this.#workspace.views.configuration.properties['structurizr.sort'] === 'type')) {
                listOfViews.sort(function (a, b) {
                    return (a.viewTypeOrder + a.scope + a.key).localeCompare(b.viewTypeOrder + b.scope + b.key);
                });
            } else if (this.#workspace.views.configuration.properties && this.#workspace.views.configuration.properties['structurizr.sort'] === 'created') {
                listOfViews.sort(function (a, b) {
                    return a.view.order - b.view.order;
                });
            } else {
                listOfViews.sort(function (a, b) {
                    if (a.softwareSystem === undefined && b.softwareSystem !== undefined) {
                        return -1;
                    }

                    if (b.softwareSystem === undefined && a.softwareSystem !== undefined) {
                        return 1;
                    }

                    if (a.softwareSystem === undefined && b.softwareSystem === undefined) {
                        return (a.viewTypeOrder + a.key).localeCompare(b.viewTypeOrder + b.key);
                    }

                    if (a.softwareSystem.name.localeCompare(b.softwareSystem.name) === 0) {
                        return (a.viewTypeOrder + '.' + a.elementTypeOrder + '.' + a.scope + a.key).localeCompare(b.viewTypeOrder + '.' + b.elementTypeOrder + '.' + b.scope + b.key);
                    } else {
                        return a.softwareSystem.name.localeCompare(b.softwareSystem.name);
                    }
                });
            }
        } catch (err) {
            console.log(err);
        }

        this.#views = listOfViews.map(function(v) {
            return v.view;
        });
    };

    #findElementForView(view) {
        if (view.type === structurizr.constants.CUSTOM_VIEW_TYPE) {
            return undefined;
        } else if (view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE) {
            return undefined;
        } else if (view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE) {
            return this.findElementById(view.softwareSystemId);
        } else if (view.type === structurizr.constants.CONTAINER_VIEW_TYPE) {
            return this.findElementById(view.softwareSystemId);
        } else if (view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {
            return this.findElementById(view.containerId);
        } else if (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
            if (view.elementId !== undefined) {
                return this.findElementById(view.elementId);
            } else {
                return undefined;
            }
        } else if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
            if (view.softwareSystemId !== undefined) {
                return this.findElementById(view.softwareSystemId);
            } else {
                return undefined;
            }
        } else if (view.type === structurizr.constants.IMAGE_VIEW_TYPE) {
            if (view.elementId !== undefined) {
                return this.findElementById(view.elementId);
            } else {
                return undefined;
            }
        }
    }

    copyLayoutFrom(views, viewKey) {
        if (views === undefined) {
            return;
        }

        this.#copyLayoutFromViews(views.customViews, viewKey);
        this.#copyLayoutFromViews(views.systemLandscapeViews, viewKey);
        this.#copyLayoutFromViews(views.systemContextViews, viewKey);
        this.#copyLayoutFromViews(views.containerViews, viewKey);
        this.#copyLayoutFromViews(views.componentViews, viewKey);
        this.#copyLayoutFromViews(views.dynamicViews, viewKey);
        this.#copyLayoutFromViews(views.deploymentViews, viewKey);
    }

    #copyLayoutFromViews(views, viewKey) {
        if (views !== undefined) {
            for (var i = 0; i < views.length; i++) {
                const v = views[i];

                if (viewKey === undefined || viewKey === v.key) {
                    var view = this.findViewByKey(v.key);
                    view.elements = v.elements;
                    view.relationships = v.relationships;

                    if (view.elements === undefined) {
                        view.elements = [];
                    }

                    if (view.relationships === undefined) {
                        view.relationships = [];
                    }

                    if (v.paperSize) {
                        view.paperSize = v.paperSize;
                    }

                    if (v.dimensions) {
                        view.dimensions = v.dimensions;
                    }
                }
            }
        }
    }

    hasStyles() {
        return this.#workspace.views.configuration.styles.elements.length > 0 || this.#workspace.views.configuration.styles.relationships.length > 0;
    }

    findElementStyleByTag(tag) {
        var style = undefined;

        this.#workspace.views.configuration.styles.elements.forEach(function(elementStyle) {
            if (elementStyle.tag === tag) {
                style = elementStyle;
            }
        })

        return style;
    }

    findViewByKey(key) {
        var view = undefined;
        this.#allViews.forEach(function(v) {
            if (v.key === key) {
                view = v;
            }
        });

        return view;
    };

    #getTerminology(key, defaultValue) {
        if (this.#workspace.views.configuration.terminology.hasOwnProperty(key)) {
            return this.#workspace.views.configuration.terminology[key];
        } else {
            return defaultValue;
        }
    }

    getTerminologyFor(item) {
        if (item.type === 'Person') {
            return this.#getTerminology('person', 'Person');
        } else if (item.type === 'SoftwareSystem' || item.type === "SoftwareSystemInstance") {
            return this.#getTerminology('softwareSystem', 'Software System');
        } else if (item.type === 'Container' || item.type === "ContainerInstance") {
            return this.#getTerminology('container', 'Container');
        } else if (item.type === 'Component') {
            return this.#getTerminology('component', 'Component');
        } else if (item.type === 'DeploymentNode') {
            return this.#getTerminology('deploymentNode', 'Deployment Node');
        } else if (item.type === 'InfrastructureNode') {
            return this.#getTerminology('infrastructureNode', 'Infrastructure Node');
        } else if (item.type === "Enterprise") {
            return this.#getTerminology('enterprise', 'Enterprise');
        } else if (item.sourceId && item.destinationId) {
            return this.#getTerminology('relationship', 'Relationship');
        }

        return '';
    }

    #sortArrayByNameAscending(array) {
        array.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        })
    }

    #sortProperties(modelItem) {
        if (modelItem.properties) {
            var orderedProperties = {};
            Object.keys(modelItem.properties).sort().forEach(function(key) {
                orderedProperties[key] = modelItem.properties[key];
            });

            modelItem.properties = orderedProperties;
        }
    }

}