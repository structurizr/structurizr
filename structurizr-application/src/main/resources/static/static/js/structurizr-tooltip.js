structurizr.ui.Tooltip = function() {
    var visible = false;
    var enabled = false;

    var tooltip = $('#tooltip');
    var tooltipHeader = $('#tooltipHeader');
    var tooltipName = $('#tooltipName');
    var tooltipParent = $('#tooltipParent');
    const tooltipHr = $('#tooltip hr');
    var tooltipDescription = $('#tooltipDescription');
    var tooltipMetadata = $('#tooltipMetadata');
    var tooltipTags = $('#tooltipTags');
    var tooltipProperties = $('#tooltipProperties');
    var tooltipUrl = $('#tooltipUrl');
    var tooltipAdditionalContent = $('#tooltipAdditionalContent');

    this.reposition = function(x, y) {
        var windowWidth = window.innerWidth;
        var windowHeight = window.innerHeight;
        var tooltipWidth = tooltip.outerWidth(true);
        var tooltipHeight = tooltip.outerHeight(true);

        if ((x + tooltipWidth) < windowWidth) {
            // do nothing
        } else {
            x = x - tooltipWidth;
        }

        if ((y + tooltipHeight) < windowHeight) {
            // do nothing
        } else {
            y = y - tooltipHeight;
        }

        tooltip.css({left: Math.max(0, x), top: Math.max(0, y)});
    };

    this.isEnabled = function() {
        return enabled;
    }

    this.enable = function() {
        enabled = true;
    }

    this.disable = function() {
        enabled = false;
        this.hide();
    }

    this.hide = function() {
        tooltip.addClass("hidden");
        visible = false;
    };

    function show() {
        tooltip.removeClass("hidden");
        visible = true;
    }

    this.showTooltip = function(name, description, metadata, background, color) {
        tooltipName.html(structurizr.util.escapeHtml(name));
        tooltipDescription.html(structurizr.util.escapeHtml(description));
        tooltipMetadata.html(structurizr.util.escapeHtml(metadata));

        tooltip.css("background", background);
        tooltip.css("border-color", structurizr.util.shadeColor(background, -20));
        tooltip.css("color", color);
        tooltip.css('border-style', 'solid');

        show();
        this.reposition(0, 0);
    };

    this.showTooltipForElement = function(element, style, x, y, header, perspective) {
        if (element === undefined) {
            return;
        }

        if (header) {
            var parent = '';
            if (element.parentId) {
                const parentElement = structurizr.workspace.findElementById(element.parentId);
                parent = 'Parent: ' + parentElement.name + ' [' + structurizr.workspace.getTerminologyFor(parentElement) + ']';
            }

            renderHeader(
                structurizr.util.escapeHtml(element.name),
                structurizr.ui.getMetadataForElement(element, true),
                parent,
                element.description ? structurizr.util.escapeHtml(element.description).replaceAll('\n', '<br />') : ''
            );
        } else {
            tooltipHeader.addClass('hidden');
        }

        if (perspective === undefined) {
            renderTags(structurizr.workspace.getAllTagsForElement(element));
            renderProperties(structurizr.workspace.getAllPropertiesForElement(element));
            renderUrl(element.url);
            renderAdditionalContent('');
        } else {
            renderTags([]);
            renderUrl(undefined);
            renderProperties({});

            var additionalContent = '';
            var perspectiveDetails = undefined;

            if (element.perspectives) {
                element.perspectives.forEach(function(p) {
                    if (p.name === perspective) {
                        perspectiveDetails = p;
                    }
                });
            }

            if (perspectiveDetails === undefined) {
                if (element.type === 'SoftwareSystemInstance') {
                    var softwareSystem = structurizr.workspace.findElementById(element.softwareSystemId);
                    if (softwareSystem.perspectives) {
                        softwareSystem.perspectives.forEach(function(p) {
                            if (p.name === perspective) {
                                perspectiveDetails = p;
                            }
                        });
                    }
                } else if (element.type === 'ContainerInstance') {
                    var container = structurizr.workspace.findElementById(element.containerId);
                    if (container.perspectives) {
                        container.perspectives.forEach(function(p) {
                            if (p.name === perspective) {
                                perspectiveDetails = p;
                            }
                        });
                    }
                }
            }

            if (perspectiveDetails !== undefined) {
                additionalContent += renderPerspective(perspectiveDetails);
            }

            renderAdditionalContent(additionalContent);
        }

        show();
        this.reposition(x, y);

        if (style) {
            tooltip.css("background", style.background);
            tooltip.css("border-color", style.stroke);
            tooltip.css("color", style.color);
            if (style.borderStyle === 'Dashed') {
                tooltip.css('border-style', 'dashed');
            } else if (style.borderStyle === 'Dotted') {
                tooltip.css('border-style', 'dotted');
            } else {
                tooltip.css('border-style', 'solid');
            }

            $('#tooltip .tag').css("border-color", style.color);
            $('#tooltip a').css("color", style.color);
            $('#tooltip a').css("text-decoration", "underline");
            $('#tooltip hr').css("border-color", style.stroke);
        } else {
            tooltip.css("background", '#ffffff');
            tooltip.css("border-color", '#000000');
            tooltip.css("color", '#000000');
            tooltip.css('border-style', 'solid');
            $('#tooltip .tag').css("border-color", '#000000');
            $('#tooltip a').css("color", '#000000');
            $('#tooltip a').css("text-decoration", "underline");
            $('#tooltip hr').css("border-color", '#000000');
        }
    };

    this.showTooltipForRelationship = function(relationship, relationshipInView, style, x, y, header, perspective) {
        if (relationship === undefined) {
            return;
        }

        if (relationshipInView === undefined) {
            relationshipInView = {};
        }

        const darkMode = structurizr.ui.isDarkMode();
        tooltip.css("background", (darkMode === true ? structurizr.ui.DARK_MODE_DEFAULTS.background : structurizr.ui.LIGHT_MODE_DEFAULTS.background));

        if (header) {
            var relationshipSummary = relationshipInView.description;
            if (relationshipSummary === undefined) {
                relationshipSummary = relationship.description;
            }
            if (relationshipSummary === undefined || relationshipSummary.length === 0) {
                relationshipSummary = '';
            }

            var description = '';
            description += '<p style="font-weight: bold">';
            description += structurizr.util.escapeHtml(structurizr.workspace.findElementById(relationship.sourceId).name);
            description += ' <span style="color: gray;">--</span> ';
            description += structurizr.util.escapeHtml(relationshipSummary);
            description += ' <span style="color: gray;">-&gt;</span> ';
            description += structurizr.util.escapeHtml(structurizr.workspace.findElementById(relationship.destinationId).name);
            description += '</p>';
            tooltipDescription.html(description);

            renderHeader(
                (relationshipInView.order ? relationshipInView.order + ': ' : '') + relationshipSummary,
                '[' + structurizr.workspace.getTerminologyFor(relationship) + ']',
                '',
                description
            )

            tooltipHeader.removeClass('hidden');
        } else {
            tooltipHeader.addClass('hidden');
        }

        if (perspective === undefined) {
            renderTags(structurizr.workspace.getAllTagsForRelationship(relationship));
            renderProperties(structurizr.workspace.getAllPropertiesForRelationship(relationship));
            renderUrl(relationship.url);
            renderAdditionalContent('');
        } else {
            renderTags([]);
            renderUrl(undefined);
            renderProperties({});

            var additionalContent = '';
            var perspectiveDetails = undefined;

            if (relationship.perspectives) {
                relationship.perspectives.forEach(function(p) {
                    if (p.name === perspective) {
                        perspectiveDetails = p;
                    }
                });
            }

            if (perspectiveDetails === undefined) {
                if (relationship.linkedRelationshipId) {
                    var linkedRelationship = structurizr.workspace.findRelationshipById(relationship.linkedRelationshipId);
                    if (linkedRelationship && linkedRelationship.perspectives) {
                        linkedRelationship.perspectives.forEach(function(p) {
                            if (p.name === perspective) {
                                perspectiveDetails = p;
                            }
                        });
                    }
                }
            }

            if (perspectiveDetails !== undefined) {
                additionalContent += renderPerspective(perspectiveDetails);
            }

            renderAdditionalContent(additionalContent);
        }

        show();
        this.reposition(x, y);

        var color = style.color;
        if (color === undefined) {
            color = (darkMode === true ? structurizr.ui.DARK_MODE_DEFAULTS.color : structurizr.io.LIGHT_MODE_DEFAULTS.color);
        }

        tooltip.css("border-color", color);
        tooltip.css("color", color);
        $('#tooltip .tag').css("border-color", color);
        $('#tooltip a').css("color", color);
        $('#tooltip a').css("text-decoration", "underline");
        $('#tooltip hr').css("border-color", color);

        tooltip.css('border-style', style.lineStyle);
    }

    this.isVisible = function() {
        return visible;
    }

    function renderHeader(name, metadata, parent, description) {
        tooltipName.html(name);
        tooltipMetadata.text(metadata);

        if (parent && parent.length > 0) {
            tooltipParent.text(parent);
            tooltipParent.removeClass('hidden');
        } else {
            tooltipParent.text('');
            tooltipParent.addClass('hidden');
        }

        tooltipDescription.html(description);

        tooltipHeader.removeClass('hidden');
    }

    function renderTags(tags) {
        if (tags.length > 0) {
            var tagsHtml = '<div class="smaller">';

            tags.forEach(function (tag) {
                if (tag !== undefined) {
                    tag = tag.trim();
                    if (tag.length > 0) {
                        tagsHtml += '<span class="tag">';
                        tagsHtml += structurizr.util.escapeHtml(tag);
                        tagsHtml += '</span>';
                    }
                }
            });

            tagsHtml += '</div>';

            tooltipTags.html(tagsHtml);
            tooltipTags.removeClass('hidden');
        } else {
            tooltipTags.html('');
            tooltipTags.addClass('hidden');
        }
    }

    function renderProperties(properties) {
        var propertiesHtml = '';

        if (Object.keys(properties).length > 0) {
            Object.keys(properties).forEach(function (key) {
                if (key.indexOf('structurizr.') === 0) {
                    // ignore 'structurizr.' properties
                } else {
                    var value = properties[key];
                    if (isUrl(value)) {
                        value = '<a href="' + structurizr.util.escapeHtml(value) + '" target="_blank">' + structurizr.util.escapeHtml(value) + '</a>';
                    } else {
                        value = structurizr.util.escapeHtml(value);
                    }
                    propertiesHtml += '<li>';
                    propertiesHtml += (structurizr.util.escapeHtml(key) + ' = ' + value);
                    propertiesHtml += '</li>';
                }
            });
        }

        if (propertiesHtml.length > 0) {
            tooltipProperties.html('<div class="smaller"><div>Properties:</div><ul>' + propertiesHtml + '</ul></div>');
            tooltipProperties.removeClass('hidden');
        } else {
            tooltipProperties.html('');
            tooltipProperties.addClass('hidden');
        }
    }

    function renderUrl(url) {
        if (url && url.trim().length > 0) {
            var urlHtml = '';
            urlHtml += '<div class="smaller">';
            urlHtml += '<div>URL: ';
            urlHtml += '<a href="' + structurizr.util.escapeHtml(url) + '" target="_blank">' + structurizr.util.escapeHtml(url) + '</a>';
            urlHtml += '</div>';
            urlHtml += '</div>';

            tooltipUrl.html(urlHtml);
            tooltipUrl.removeClass('hidden');
        } else {
            tooltipUrl.html('');
            tooltipUrl.addClass('hidden');
        }
    }

    function renderAdditionalContent(content) {
        if (content && content.length > 0) {
            tooltipAdditionalContent.html(content);
            tooltipAdditionalContent.removeClass('hidden');
        } else {
            tooltipAdditionalContent.html('');
            tooltipAdditionalContent.addClass('hidden');
        }
    }

    function renderPerspective(perspective) {
        var html = '';

        var perspectiveDescription = perspective.description;
        if (perspectiveDescription === undefined) {
            perspectiveDescription = '';
        }
        perspectiveDescription = structurizr.util.escapeHtml(perspectiveDescription).replaceAll('\n', '<br />');

        html += '<div style="font-weight: bold; margin-bottom: 10px;">Perspective: ';
        html += structurizr.util.escapeHtml(perspective.name);
        html += '</div>';

        if (perspectiveDescription && perspectiveDescription.length > 0) {
            html += '<div style="margin-bottom: 10px;">';
            html += perspectiveDescription;
            html += '</div>';
        }

        if (perspective.url && perspective.url.length > 0) {
            html += '<div>';
            html += 'URL: <a href="' + perspective.url + '" target="_blank">' + perspective.url + '</a>';
            html += '</div>';
        }

        if (perspective.value !== undefined && perspective.value.length > 0) {
            html += '<div>';
            html += 'Value: ' + perspective.value;
            html += '</div>';
        }

        return html;
    }

    function isUrl(s) {
        return s !== undefined && (s.indexOf('https://') === 0 || s.indexOf('http://') === 0);
    }

};

const tooltip = new structurizr.ui.Tooltip();