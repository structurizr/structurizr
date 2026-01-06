<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<script type="text/javascript" src="/static/js/d3-7.8.2.min.js"></script>
<div id="exploreGraphPanel">

    <div id="exploreGraph" style="width: 100%"></div>
    <div style="position: absolute; bottom: 10px; left: 10px;">
        <c:choose>
        <c:when test="${embed}">
            <div id="graphTitle" style="color: #aaaaaa; font-size: 13px; user-select: none; -moz-user-select: none; -khtml-user-select: none; -webkit-user-select: none; -o-user-select: none;"></div>
        </c:when>
        <c:otherwise>
        <select id="viewSelector" class="form-select"></select>
        <script nonce="${scriptNonce}">
            $('#viewSelector').change(function() { showSelectedView(); });
        </script>
        </c:otherwise>
        </c:choose>
    </div>

    <%@ include file="/WEB-INF/fragments/tooltip.jspf" %>
    <%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

    <div id="embeddedControls" style="text-align: right; position: absolute; bottom: 10px; right: 10px; opacity: 0.1; z-index: 100;">
        <c:if test="${workspace.id > 0 && (embed eq true && workspace.editable eq false)}">
        <button id="openGraphInNewWindowButton" class="btn btn-default" title="Open graph in new window"><img src="/static/bootstrap-icons/link.svg" class="icon-btn" /></button>
        <script nonce="${scriptNonce}">
            $('#openGraphInNewWindowButton').click(function() { openGraphInNewWindow(); });
        </script>
        </c:if>
        <div class="btn-group">
            <button id="zoomOutButton" class="btn btn-default" title="Decrease distance [-]"><img src="/static/bootstrap-icons/zoom-out.svg" class="icon-btn" /></button>
            <button id="zoomInButton" class="btn btn-default" title="Increase distance [+]"><img src="/static/bootstrap-icons/zoom-in.svg" class="icon-btn" /></button>
        </div>
        <button id="enterFullScreenButton" class="btn btn-default" title="Enter Full Screen [f]"><img src="/static/bootstrap-icons/fullscreen.svg" class="icon-btn" /></button>
        <button id="exitFullScreenButton" class="btn btn-default hidden" title="Exit Full Screen [Escape]"><img src="/static/bootstrap-icons/fullscreen-exit.svg" class="icon-btn" /></button>
    </div>
    <script nonce="${scriptNonce}">
        $('#enterFullScreenButton').click(function() { structurizr.ui.enterFullScreen('exploreGraphPanel'); });
        $('#exitFullScreenButton').click(function() { structurizr.ui.exitFullScreen(); });
        $('#zoomOutButton').click(function() { decreaseDistance(); });
        $('#zoomInButton').click(function() { increaseDistance(); });
    </script>
</div>

<script nonce="${scriptNonce}">
    var margin = 0;
    var width;
    var height;
    const distanceDelta = 50;

    var graph;
    var simulation;
    var distance;
    var node;
    var link;
    var nodeHighlighted;

    var viewKey = decodeURIComponent('<c:out value="${view}" />');
    var view;

    const relationshipsBySourceAndDestination = [];

    const nodeSizes = {};
    nodeSizes[structurizr.constants.CUSTOM_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.PERSON_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE] = 20;
    nodeSizes[structurizr.constants.CONTAINER_ELEMENT_TYPE] = 17;
    nodeSizes[structurizr.constants.COMPONENT_ELEMENT_TYPE] = 11;


    progressMessage.show('<p>Loading workspace...</p>');

    function workspaceLoaded() {
        structurizr.ui.loadThemes(function() {
            init();
        });
    }

    function init() {
        setWidthAndHeight();

        if (viewKey.length === 0) {
            if (window.location.hash.length > 1) {
                viewKey = decodeURIComponent(window.location.hash.substring(1));
            } else {
                viewKey = structurizr.workspace.getViews()[0].key;
            }
        }

        createGraph();
        renderGraph();
        addEventHandlers();

        <c:choose>
        <c:when test="${embed}">
        $('#graphTitle').text(structurizr.ui.getTitleForView(view));
        </c:when>
        <c:otherwise>
        const views = structurizr.workspace.getViews();
        views.forEach(function(view) {
            if (
                view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {
                $('#viewSelector').append(
                    $('<option></option>').val(view.key).html(structurizr.ui.getTitleForView(view))
                );
            }
        });
        $('#viewSelector').val(viewKey);
        </c:otherwise>
        </c:choose>

        structurizr.ui.applyBranding();
        $('#brandingLogoAnchor').attr('href', '<c:out value="${urlPrefix}" />');
        progressMessage.hide();
    }

    function createGraph() {
        graph = { nodes: [], links: []};
        const elements = [];
        const elementIds = [];
        quickNavigation.clear();

        if (viewKey !== undefined) {
            view = structurizr.workspace.findViewByKey(viewKey);
            if (view !== undefined) {
                if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                    // find the base view instead
                    view = structurizr.workspace.findViewByKey(view.baseViewKey);
                }

                if (view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                    view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                    view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {

                    view.elements.forEach(function(elementView) {
                        const element = structurizr.workspace.findElementById(elementView.id);
                        if (element) {
                            elements.push(element);
                            elementIds.push(element.id);
                        }
                    });
                }
            } else {
                return;
            }
        }

        elements.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });

        elements.forEach(function(element) {
            const elementStyle = structurizr.ui.findElementStyle(element, structurizr.ui.isDarkMode());
            graph.nodes.push({
                id: element.id,
                name: element.name,
                description: element.description,
                style: elementStyle,
                type: element.type,
                tags: element.tags,
                element: element
            });

            registerRelationship(element.id, element.id); // this prevents the nodes in the graph linking to themselves

            const elementType = structurizr.workspace.getTerminologyFor(element);
            const html = '<span class="label smaller" style="background: ' + elementStyle.background + '; border: solid 1px ' + elementStyle.stroke + '; color: ' + elementStyle.color + '; margin-right: 10px"> ' + structurizr.util.escapeHtml(elementType) + '</span>' + structurizr.util.escapeHtml(element.name);
            quickNavigation.addHandler(html, function() {
                highlightNode(element.id);
            });

            if (element.relationships) {
                element.relationships.forEach(function(relationship) {
                    if (elementIds.indexOf(relationship.sourceId) > -1 && elementIds.indexOf(relationship.destinationId) > -1) {
                        registerRelationship(relationship.sourceId, relationship.destinationId);

                        graph.links.push({
                            id: relationship.id,
                            source: relationship.sourceId,
                            target: relationship.destinationId,
                            type: 'Relationship',
                            description: relationship.description,
                            style: structurizr.ui.findRelationshipStyle(relationship, structurizr.ui.isDarkMode()),
                            relationship: relationship
                        });
                    }
                });
            }
        });
    }

    function renderGraph() {
        $("#exploreGraph").height(height);
        $("#exploreGraph").empty();

        if (view === undefined) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">No view with key <code>' + viewKey + '</code>.</div>');
            return;
        }

        if (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">Graphs are not available for dynamic views.</div>');
            return;
        }

        if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
            $("#exploreGraph").html('<div style="text-align: center; vertical-align: center; margin-top: 200px">Graphs are not available for deployment views.</div>');
            return;
        }

        var svg = d3.select("#exploreGraph").append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g");

        var endMarkerColors = [];

        graph.links.forEach(function(link) {
            var color = link.style.color.toLowerCase().replace('#', '');

            if (endMarkerColors.indexOf(color) === -1) {
                svg.append("svg:defs").selectAll("marker")
                    .data(["arrow" + color])
                    .enter().append("svg:marker")
                    .attr("id", String)
                    .attr("viewBox", "0 0 12 12")
                    .attr("refX", 32)
                    .attr("refY", 6)
                    .attr("markerWidth", 12)
                    .attr("markerHeight", 12)
                    .attr("orient", "auto")
                    .append("svg:path")
                    .attr("d", "M0,0 L12,6 L0,12 L0,0")
                    .attr("fill", link.style.color);

                endMarkerColors.push(color);
            }
        });

        distance = Math.min(width, height) / 5;

        simulation = d3.forceSimulation(graph.nodes)
            .force("link", d3.forceLink().distance(distance).id(function(d) { return d.id; }))
            .force("charge", d3.forceManyBody().strength(-distance))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .stop();

        link = svg.selectAll(".link")
            .data(graph.links)
            .enter()
            .append("svg:path")
            .attr("class", "link")
            .attr("stroke", function(d) {
                return d.style.color;
            })
            .attr("id", function(d) {
                return 'relationship' + d.id;
            })
            .attr("marker-end", function(d) {
                return "url(#arrow" + d.style.color.toLowerCase().replace('#', '') + ")";
            })
            .on("mouseover", showTooltipForRelationship)
            .on("mousemove", moveTooltip)
            .on("mouseout", hideTooltip);

        link.filter(function(d) {
            return d.style.dashed === true;
        }).attr("stroke-dasharray", function(d) {
            return (d.style.dashed === true) ? '4' : '0';
        });

        node = svg.selectAll(".node")
            .data(graph.nodes)
            .enter()
            .append("g")
            .attr("class", "node")
            .call(d3.drag()
                .on("start", dragStarted)
                .on("drag", dragged)
                .on("end", dragEnded)
            )
            .on("click", click)
            .on("mouseover", showTooltipForElement)
            .on("mousemove", moveTooltip)
            .on("mouseout", hideTooltip);

        node.append("circle")
            .attr("id", function(d) {
                return 'element' + d.id;
            })
            .attr("r", function(d) {
                return nodeSizes[d.type];
            })
            .style("fill", function(d) {
                return d.style.background;
            })
            .style("stroke-width", function(d) {
                return Math.max(2, d.style.strokeWidth / 2);
            })
            .style("stroke", function(d) {
                return d.style.stroke;
            });

        node.append("text")
            .attr("class", "nodeName")
            .attr("dy", function(d) {
                return 0;
            })
            .attr("dx", function(d) {
                return nodeSizes[d.type] + 5;
            })
            .text(function (d) { return d.name; });

        node.append("text")
            .attr("class", "nodeMetadata")
            .attr("dy", function(d) {
                return 10;
            })
            .attr("dx", function(d) {
                return nodeSizes[d.type] + 5;
            })
            .text(function (d) { return '[' + structurizr.workspace.getTerminologyFor(d.element) + ']'; });

        node.filter(function(d) {
            return d.style.icon !== undefined;
        }).append("image")
            .attr("width", function(d) {
                return nodeSizes[d.type]*1.5;
            })
            .attr("height", function(d) {
                return nodeSizes[d.type]*1.5;
            })
            .attr("x", function(d) {
                return -(nodeSizes[d.type]*1.5)/2;
            })
            .attr("y", function(d) {
                return -(nodeSizes[d.type]*1.5)/2;
            })
            .attr("href", function(d) {
                return d.style.icon;
            });

        simulation.nodes(graph.nodes).on("tick", ticked);

        simulation.force("link").links(graph.links);

        simulation.alphaTarget(0.3);

        for (var i = 0; i < 300; ++i) {
            simulation.tick();
        }

        simulation.alphaTarget(0).restart();

        function ticked() {
            link.attr("d", function(d) {
                var links = getLinks(d.source, d.target);
                if ((hasRelationship(d.source.id, d.target.id) && hasRelationship(d.target.id, d.source.id)) || links.length > 1) {
                    // bi-directional or multiple relationships
                    var dx = d.target.x - d.source.x,
                        dy = d.target.y - d.source.y,
                        dr = Math.sqrt(dx * dx + dy * dy) * 2;

                    var delta = (dr / (links.length + 1));
                    dr -= Math.sqrt(links.indexOf(d.relationship) + 1) * (delta * 1.25);

                    return "M " +
                        d.source.x + "," +
                        d.source.y +
                        " A " +
                        dr + "," + dr + " 0 0,1 " +
                        d.target.x + "," +
                        d.target.y;
                } else {
                    // uni-directional relationship
                    return "M" + d.source.x + "," + d.source.y + " " + "L" + d.target.x + "," + d.target.y;
                }

            });

            node.attr("transform", function(d) {
                return "translate(" + Math.max(margin, Math.min(width - margin, d.x)) + "," + Math.max(margin, Math.min(height - margin, d.y)) + ")";
            });
        }
    }

    function showTooltipForElement(event, d) {
        tooltip.showTooltipForElement(d.element, d.style, event.pageX, event.pageY, true, undefined);
    }

    function showTooltipForRelationship(event, d) {
        tooltip.showTooltipForRelationship(d.relationship, {}, d.style, event.pageX, event.pageY, true, undefined);
    }

    function moveTooltip(event) {
        tooltip.reposition(event.pageX, event.pageY);
    }

    function hideTooltip() {
        tooltip.hide();
    }

    function showConnectedNodes(nodeClicked, reset) {
        if (reset === true) {
            node.style("stroke-width", "2px");
            node.style("opacity", '1');
            link.style("opacity", '1');

            if (!nodeHighlighted) {
                // do nothing
            } else if (nodeHighlighted === nodeClicked) {
                // the same node has been clicked again, so return everything to it's normal state
                nodeHighlighted = undefined;

                return;
            }

            nodeHighlighted = nodeClicked;
        } else {
            nodeHighlighted = undefined;
        }

        $(nodeClicked).css("stroke-width", "4px");

        // reduce the opacity of all but the neighbouring nodes
        var d = d3.select(nodeClicked).node().__data__;

        node.style("opacity", function (o) {
            var currentOpacity = $('#element' + o.id).css('opacity');
            return hasRelationship(d.id, o.id) || hasRelationship(o.id, d.id) ? 1 : (reset === true ? 0.1 : currentOpacity);
        });

        link.style("opacity", function (o) {
            var currentOpacity = $('#relationship' + o.id).css('opacity');
            return (d.id === o.source.id || d.id === o.target.id) ? 1 : (reset === true ? 0.1 : currentOpacity);
        });
    }

    function click() {
        showConnectedNodes(this, true);
    }

    function setWidthAndHeight() {
        var navHeight = 0;

        <c:if test="${empty iframe}">
        if (structurizr.ui.isFullScreen()) {
            navHeight = 0;
        } else {
            navHeight = $('#topNavigation').outerHeight();
        }
        </c:if>

        width = window.innerWidth - margin;
        height = window.innerHeight - navHeight - margin;
        distance = Math.min(width, height) / 4;
    }

    function addEventHandlers() {
        $(document).keypress(function(e) {
            const plus = 43;
            const equals = 61;
            const minus = 45;

            if (e.which === plus || e.which === equals) {
                increaseDistance();
            } else if (e.which === minus) {
                decreaseDistance();
            }
        });

        window.addEventListener("resize", function() {
            setWidthAndHeight();

            $("#exploreGraph").height(height);
            $("#exploreGraph svg").attr('width', width);
            $("#exploreGraph svg").attr('height', height);

            simulation.force("center")
                .x(width / 2)
                .y(height / 2);

            simulation.force("link").distance(distance);
            simulation.alpha(0.3).restart();
        }, false);
    }

    function increaseDistance() {
        // increase the distance between nodes
        distance = Math.min(distance + distanceDelta, Math.min(width, height)/2);
        simulation.force("link").distance(distance);
        simulation.alpha(0.3).restart();
    }

    function decreaseDistance() {
        // decrease the distance between nodes
        distance = Math.max(distance - distanceDelta, 100);
        simulation.force("link").distance(distance);
        simulation.alpha(0.3).restart();
    }

    function getLinks(source, target) {
        const links = [];
        for (var i = 0; i < graph.links.length; ++i){
            if (graph.links[i].source.id === source.id && graph.links[i].target.id === target.id) {
                links.push(graph.links[i].relationship);
            }
        }
        return links;
    }

    function dragStarted(event, d) {
        if (!event.active) {
            simulation.alphaTarget(0.3).restart();
        }
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragEnded(event, d) {
        if (!event.active) {
            simulation.alphaTarget(0);
        }
        d.fx = null;
        d.fy = null;
    }

    function highlightNode(elementId) {
        node.filter(function(d) {
            return d.element.id === elementId;
        }).nodes().forEach(function (d, i) {
            showConnectedNodes(d, true);
        });
    }

    function hasRelationship(sourceId, destinationId) {
        const key = sourceId + "," + destinationId;
        return relationshipsBySourceAndDestination.indexOf(key) > -1;
    }

    function registerRelationship(sourceId, destinationId) {
        const key = sourceId + "," + destinationId;
        relationshipsBySourceAndDestination.push(key);
    }

    function isRendered() {
        return true;
    }

    function isExportable() {
        return false;
    }

    $('#embeddedControls').hover(
        function() {
            $('#embeddedControls').css('opacity', '1.0');
        },
        function() {
            $('#embeddedControls').css('opacity', '0.1');
        }
    );

    function openGraphInNewWindow() {
        window.open('<c:out value="${urlPrefix}" />/explore/graph#' + encodeURIComponent(viewKey));
    }

    $(document).bind('webkitfullscreenchange mozfullscreenchange fullscreenchange fullscreenChange MSFullscreenChange',function(){
        if (structurizr.ui.isFullScreen()) {
            $('#enterFullScreenButton').addClass("hidden");
            $('#exitFullScreenButton').removeClass("hidden");
        } else {
            $('#enterFullScreenButton').removeClass("hidden");
            $('#exitFullScreenButton').addClass("hidden");
        }
    });

    function showSelectedView() {
        const selectedView = $('#viewSelector').val();
        viewKey = selectedView;
        createGraph();
        renderGraph();
        window.location.hash = viewKey;
    }
</script>

<c:choose>
    <c:when test="${loadWorkspaceFromParent eq true}">
        <script nonce="${scriptNonce}">
            loadWorkspaceFromParent();
        </script>
    </c:when>
    <c:when test="${not empty workspaceAsJson}">
        <%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
        <%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>