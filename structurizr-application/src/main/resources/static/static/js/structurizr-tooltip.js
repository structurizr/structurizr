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
            tooltipName.html(structurizr.util.escapeHtml(element.name));
            tooltipMetadata.text(structurizr.ui.getMetadataForElement(element, true));

            if (element.parentId) {
                var parentElement = structurizr.workspace.findElementById(element.parentId);
                tooltipParent.text('from ' + parentElement.name + ' [' + structurizr.workspace.getTerminologyFor(parentElement) + ']');
            } else {
                tooltipParent.text('');
            }

            tooltipDescription.html(element.description ? structurizr.util.escapeHtml(element.description).replaceAll('\n', '<br />') : '');

            tooltipHeader.removeClass('hidden');
        } else {
            tooltipHeader.addClass('hidden');
        }

        if (perspective === undefined) {
            var tagsHtml = '';
            var tags = structurizr.workspace.getAllTagsForElement(element);
            tagsHtml += '<div class="smaller">';
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

            renderProperties(structurizr.workspace.getAllPropertiesForElement(element));

            var urlHtml = '';
            var url = element.url;
            if (url && url.trim().length > 0) {
                urlHtml += '<div class="smaller">';
                urlHtml += '<p>URL: ';
                urlHtml += '<a href="' + structurizr.util.escapeHtml(url) + '" target="_blank">' + structurizr.util.escapeHtml(url) + '</a>';
                urlHtml += '</p>';
                urlHtml += '</div>';
            }
            tooltipUrl.html(urlHtml);
            tooltipAdditionalContent.html('');
        } else {
            tooltipTags.html('');
            tooltipUrl.html('');
            tooltipProperties.html('');

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
                var perspectiveDescription = perspectiveDetails.description;
                if (perspectiveDescription === undefined) {
                    perspectiveDescription = '';
                }
                perspectiveDescription = structurizr.util.escapeHtml(perspectiveDescription).replaceAll('\n', '<br />');

                additionalContent = '';
                additionalContent += '<p><b>Perspective: ';
                additionalContent += structurizr.util.escapeHtml(perspectiveDetails.name);
                additionalContent += '</b></p>';
                additionalContent += '<p>';
                additionalContent += perspectiveDescription;
                additionalContent += '</p>';
            }

            tooltipAdditionalContent.html(additionalContent);
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

            tooltipName.text((relationshipInView.order ? relationshipInView.order + ': ' : '') + relationshipSummary);
            tooltipParent.html('');
            tooltipMetadata.text('[' + structurizr.workspace.getTerminologyFor(relationship) + ']');

            var description = '';
            description += '<p style="font-weight: bold">';
            description += structurizr.util.escapeHtml(structurizr.workspace.findElementById(relationship.sourceId).name);
            description += ' <span style="color: gray;">--</span> ';
            description += structurizr.util.escapeHtml(relationshipSummary);
            description += ' <span style="color: gray;">-&gt;</span> ';
            description += structurizr.util.escapeHtml(structurizr.workspace.findElementById(relationship.destinationId).name);
            description += '</p>';
            tooltipDescription.html(description);

            tooltipHeader.removeClass('hidden');
        } else {
            tooltipHeader.addClass('hidden');
        }

        if (perspective === undefined) {
            var tagsHtml = '';
            var tags = structurizr.workspace.getAllTagsForRelationship(relationship);
            tagsHtml += '<div class="smaller">';
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

            renderProperties(structurizr.workspace.getAllPropertiesForRelationship(relationship));

            var urlHtml = '';
            var url = relationship.url;
            if (url && url.trim().length > 0) {
                urlHtml += '<div class="smaller">';
                urlHtml += '<p>URL: ';
                urlHtml += '<a href="' + structurizr.util.escapeHtml(url) + '" target="_blank">' + structurizr.util.escapeHtml(url) + '</a>';
                urlHtml += '</p>';
                urlHtml += '</div>';
            }
            tooltipUrl.html(urlHtml);
            tooltipAdditionalContent.html('');
        } else {
            var additionalContent = '';
            if (perspective !== undefined) {
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
                    var perspectiveDescription = perspectiveDetails.description;
                    if (perspectiveDescription === undefined) {
                        perspectiveDescription = '';
                    }
                    perspectiveDescription = structurizr.util.escapeHtml(perspectiveDescription).replaceAll('\n', '<br />');

                    additionalContent += '<p><b>Perspective: ';
                    additionalContent += structurizr.util.escapeHtml(perspectiveDetails.name);
                    additionalContent += '</b></p>';
                    additionalContent += '<p>';
                    additionalContent += perspectiveDescription;
                    additionalContent += '</p>';
                }
            }

            tooltipAdditionalContent.html(additionalContent);
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
            tooltipProperties.html('<div class="smaller"><p>Properties:</p><ul>' + propertiesHtml + '</ul></div>');
        } else {
            tooltipProperties.html('');
        }
    }

    function isUrl(s) {
        return s !== undefined && (s.indexOf('https://') === 0 || s.indexOf('http://') === 0);
    }

};

const tooltip = new structurizr.ui.Tooltip();