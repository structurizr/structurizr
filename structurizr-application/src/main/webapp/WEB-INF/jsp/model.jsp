<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<style>
    .model {
        margin-bottom: 200px;
        text-align: center;
    }
    .element {
        margin: 30px;
        padding: 5px;
        text-align: center;
        border-radius: 5px;
    }
    .relationships {
        border-radius: 3px;
    }
    .relationship {
        padding: 10px 5px 10px 5px;
    }
    .elementName {
        font-weight: bold;
    }
    .relationshipDescription {
        font-weight: bold;
    }
    .metadata {
        font-weight: normal;
        font-size: 75%;
    }
    .tags {
        padding-bottom: 10px;
    }
    .tag {
        font-size: 90%;
        border-width: 1px;
        border-style: solid;
        padding: 3px 3px 3px 3px;
        margin: 3px 3px 3px 0;
        border-radius: 6px;
        white-space: nowrap;
        display: inline-block;
        text-align: center;
    }
    .id {
        min-width: 20px;
    }
</style>

<div style="margin: 80px 40px 40px 40px;">
    <div id="model">
    </div>
</div>

<script nonce="${scriptNonce}">
    progressMessage.show('<p>Loading workspace...</p>');

    const DARK_MODE_COOKIE_NAME = 'structurizr.darkMode';
    var darkMode;

    function workspaceLoaded() {
        structurizr.ui.loadThemes('${structurizrConfiguration.themesUrl}', function () {
            init();
        });
    }

    function init() {
        darkMode = document.cookie.indexOf(DARK_MODE_COOKIE_NAME + '=true') > -1;
        const modelDiv = $('#model');
        var elementsHtml = '';
        elementsHtml += '<div class="model">';
        elementsHtml += '<h3>Static model</h3>';
        const elements = structurizr.workspace.getElements();

        const customs = elements.filter(function(e) {
            return e.type === structurizr.constants.CUSTOM_ELEMENT_TYPE;
        });
        customs.forEach(function(custom) {
            elementsHtml += openElement(custom);
            elementsHtml += renderElement(custom);
            elementsHtml += closeElement();
        });

        const people = elements.filter(function(e) {
            return e.type === structurizr.constants.PERSON_ELEMENT_TYPE;
        });
        people.forEach(function(person) {
            elementsHtml += openElement(person);
            elementsHtml += renderElement(person);
            elementsHtml += closeElement();
        });

        const softwareSystems = elements.filter(function(e) {
            return e.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE;
        });
        softwareSystems.forEach(function(softwareSystem) {
            elementsHtml += openElement(softwareSystem);
            elementsHtml += renderElement(softwareSystem);

            if (softwareSystem.containers) {
                softwareSystem.containers.forEach(function(container) {
                    elementsHtml += openElement(container);
                    elementsHtml += renderElement(container);

                    if (container.components) {
                        container.components.forEach(function(component) {
                            elementsHtml += openElement(component);
                            elementsHtml += renderElement(component);
                            elementsHtml += closeElement();
                        });
                    }

                    elementsHtml += closeElement();
                });
            }
            elementsHtml += closeElement();
        });
        elementsHtml += '</div>';

        var deploymentEnvironments = [];
        elements.forEach(function(element) {
            if (element.environment && deploymentEnvironments.indexOf(element.environment) === -1) {
                deploymentEnvironments.push(element.environment);
            }
        });

        deploymentEnvironments.forEach(function(deploymentEnvironment) {
            const deploymentNodes = elements.filter(function(element) {
                return element.parentId === undefined && element.environment === deploymentEnvironment;
            });
            elementsHtml += '<div class="model">';
            elementsHtml += '<h3>Deployment model: ';
            elementsHtml += deploymentEnvironment;
            elementsHtml += '</h3>';

            deploymentNodes.forEach(function(deploymentNode) {
                elementsHtml += renderDeploymentNode(deploymentNode, 0);
            });
            elementsHtml += '</div>';
        });

        modelDiv.append(elementsHtml);

        highlightModelItem();

        progressMessage.hide();
    }

    function renderDeploymentNode(deploymentNode) {
        var html = '';

        html += openElement(deploymentNode);
        html += renderElement(deploymentNode);

        deploymentNode.children.forEach(function(child) {
            html += renderDeploymentNode(child);
        });

        deploymentNode.infrastructureNodes.forEach(function(infrastructureNode) {
            html += openElement(infrastructureNode);
            html += renderElement(infrastructureNode);
            html += closeElement();
        });

        deploymentNode.softwareSystemInstances.forEach(function(softwareSystemInstance) {
            html += openElement(softwareSystemInstance);
            html += renderElement(softwareSystemInstance);
            html += closeElement();
        });

        deploymentNode.containerInstances.forEach(function(containerInstance) {
            html += openElement(containerInstance);
            html += renderElement(containerInstance);
            html += closeElement();
        });

        html += closeElement();

        return html;
    }

    function openElement(element) {
        const elementStyle = structurizr.ui.findElementStyle(element, darkMode);
        return '<div id="modelItem' + element.id + '" class="element" style="background: ' + elementStyle.background + '; border: solid 3px ' + elementStyle.stroke + '; color: ' + elementStyle.color + ';">';
    }

    function closeElement() {
        return '</div>';
    }

    function renderElement(element) {
        var elementsHtml = '';
        const name = element.name;
        const metadata = structurizr.ui.getMetadataForElement(element, true);
        const description = element.description;
        const tags = structurizr.workspace.getAllTagsForElement(element);

        elementsHtml += '<a name="' + element.id + '"></a>';

        elementsHtml += '<div style="display: inline-block; width: 50%; text-align: left; vertical-align: top; padding: 10px;">';
        elementsHtml += '<div>';
        elementsHtml += '<span class="tag id">' + element.id + '</span> ';
        elementsHtml += '<span class="elementName">';
        elementsHtml += structurizr.util.escapeHtml(name);
        elementsHtml += '</span> ';
        elementsHtml += '<span class="metadata">';
        elementsHtml += structurizr.util.escapeHtml(metadata);
        elementsHtml += '</span> ';
        elementsHtml += '</div>';

        elementsHtml += '<div class="description">';
        elementsHtml += structurizr.util.escapeHtml(description);
        elementsHtml += '</div>';
        elementsHtml += '</div>';

        elementsHtml += '<div style="display: inline-block; width: 50%; text-align: right; vertical-align: top; padding: 10px;">';
        elementsHtml += '<div class="tags">';
        tags.forEach(function(tag) {
            elementsHtml += '<span class="tag">' + tag + '</span>';
        });
        elementsHtml += '</div>';
        elementsHtml += '</div>';

        var instance = '';
        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE || element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE) {
            instance = ' (instance)';
        }
        quickNavigation.addItem(element.id + ' ' + structurizr.util.escapeHtml(metadata) + ' ' + structurizr.util.escapeHtml(name) + instance, '#' + element.id);

        elementsHtml += renderRelationships(element.relationships);

        return elementsHtml;
    }

    function renderRelationships(relationships) {
        if (relationships === undefined) {
            return '';
        }

        const background = darkMode ? '#111111' : '#ffffff';
        var relationshipsHtml = '<div class="relationships" style="background: ' + background + ';">';
        relationships.forEach(function(relationship) {
            const source = structurizr.workspace.findElementById(relationship.sourceId);
            const sourceName = source.name;
            const destination = structurizr.workspace.findElementById(relationship.destinationId);
            const destinationName = destination.name;
            const destinationMetadata = structurizr.ui.getMetadataForElement(destination, true);
            const relationshipMetadata = structurizr.ui.getMetadataForRelationship(relationship);
            const description = (relationship.description ? relationship.description : '');
            const tags = structurizr.workspace.getAllTagsForRelationship(relationship);

            const implies = structurizr.workspace.getRelationships().filter(function(r) {
                return r.linkedRelationshipId === relationship.id;
            });

            const relationshipStyle = structurizr.ui.findRelationshipStyle(relationship, false);

            relationshipsHtml += '<a name="' + relationship.id + '"></a>';
            relationshipsHtml += '<div id="modelItem' + relationship.id + '" class="relationship" style="color: ' + relationshipStyle.color + ';">';

            relationshipsHtml += '<div style="display: inline-block; width: 70%; text-align: left; vertical-align: top; padding: 5px 5px 0 5px;">';
            relationshipsHtml += '<span class="tag id">' + relationship.id + '</span>';
            relationshipsHtml += ' --- ';
            relationshipsHtml += '<span class="relationshipDescription">';
            relationshipsHtml += structurizr.util.escapeHtml(description);
            relationshipsHtml += '</span> <span class="metadata">';
            relationshipsHtml += structurizr.util.escapeHtml(relationshipMetadata);
            relationshipsHtml += '</span> --&gt; <span class="elementName">';
            relationshipsHtml += '<a href="#' + destination.id + '">';
            relationshipsHtml += structurizr.util.escapeHtml(destinationName);
            relationshipsHtml += '</a></span> <span class="metadata">';
            relationshipsHtml += structurizr.util.escapeHtml(destinationMetadata);
            relationshipsHtml += '</span>';
            relationshipsHtml += '</div> ';

            relationshipsHtml += '<div style="display: inline-block; width: 29%; text-align: right; vertical-align: top; padding: 5px 5px 0 5px;">';
            tags.forEach(function(tag) {
                relationshipsHtml += '<span class="tag">' + tag + '</span>';
            });
            relationshipsHtml += '</div> ';

            if (relationship.linkedRelationshipId !== undefined) {
                const linkedRelationship = structurizr.workspace.findRelationshipById(relationship.linkedRelationshipId);
                const linkedSource = structurizr.workspace.findElementById(linkedRelationship.sourceId);
                const linkedSourceName = linkedSource.name;
                const linkedSourceMetadata = structurizr.ui.getMetadataForElement(linkedSource, true);
                const linkedDestination = structurizr.workspace.findElementById(linkedRelationship.destinationId);
                const linkedDestinationName = linkedDestination.name;
                const linkedDestinationMetadata = structurizr.ui.getMetadataForElement(linkedDestination, true);
                const linkedRelationshipMetadata = structurizr.ui.getMetadataForRelationship(linkedRelationship);
                const linkedDescription = (linkedRelationship.description ? linkedRelationship.description : '');

                relationshipsHtml += '<div style="font-size: 90%; text-align: left; padding-left: 10px;">';
                relationshipsHtml += 'implied by <a href="#' + linkedRelationship.id + '"><span class="tag id">' + linkedRelationship.id + '</span></a> ';
                relationshipsHtml += '<span class="elementName">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedSourceName);
                relationshipsHtml += '</a></span> <span class="metadata">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedSourceMetadata);
                relationshipsHtml += '</span>';
                relationshipsHtml += ' --- ';
                relationshipsHtml += '<span class="relationshipDescription">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedDescription);
                relationshipsHtml += '</span> <span class="metadata">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedRelationshipMetadata);
                relationshipsHtml += '</span> --&gt; <span class="elementName">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedDestinationName);
                relationshipsHtml += '</a></span> <span class="metadata">';
                relationshipsHtml += structurizr.util.escapeHtml(linkedDestinationMetadata);
                relationshipsHtml += '</span>';
                relationshipsHtml += '</div> ';
            }
            if (implies.length > 0) {
                relationshipsHtml += '<div style="font-size: 90%; text-align: left; padding-left: 10px;">';
                relationshipsHtml += 'implies ';
                implies.forEach(function(r) {
                    relationshipsHtml += '<a href="#' + r.id + '"><span class="tag id">' + r.id + '</span></a> ';
                });
                relationshipsHtml += '</div> ';
            }

            relationshipsHtml += '</div> ';

            const technology = relationship.technology ? ': ' + relationship.technology : '';
            quickNavigation.addItem(relationship.id + ' [Relationship' + technology + '] ' + structurizr.util.escapeHtml(sourceName) + ' ' + structurizr.util.escapeHtml(relationship.description) + ' ' + structurizr.util.escapeHtml(destinationName), '#' + relationship.id);
        });

        relationshipsHtml += '</div> ';

        return relationshipsHtml;
    }

    $(window).on( 'hashchange', function( e ) {
        highlightModelItem();
    });

    function highlightModelItem() {
        var hash = window.location.hash;
        if (hash) {
            hash = hash.substring(1);
            if ($('#modelItem' + hash)[0]) {
                $('#modelItem' + hash)[0].scrollIntoView(true);
            }
        }
    }
</script>

<c:choose>
    <c:when test="${not empty workspaceAsJson}">
<%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
<%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>

<c:if test="${structurizrConfiguration.profile eq 'Local'}">
<%@ include file="/WEB-INF/fragments/workspace/auto-refresh.jspf" %>
</c:if>