<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<script type="text/javascript" src="/static/js/d3-7.8.2.min.js"></script>

<div id="exploreTreePanel" style="overflow-y: scroll">

    <div id="exploreTree"></div>

    <%@ include file="/WEB-INF/fragments/tooltip.jspf" %>

    <div id="embeddedControls" style="overflow: scroll; text-align: right; position: fixed; bottom: 10px; right: 10px; opacity: 0.1; z-index: 100;">
        <c:if test="${workspace.id > 0 && (embed eq true && workspace.editable eq false)}">
        <button id="openTreeInNewWindowButton" class="btn btn-default" title="Open graph in new window"><img src="/static/bootstrap-icons/link.svg" class="icon-btn" /></button>
        <script nonce="${scriptNonce}">
            $('#openTreeInNewWindowButton').click(function() { openTreeInNewWindow(); });
        </script>
        </c:if>
        <button id="enterFullScreenButton" class="btn btn-default" title="Enter Full Screen [f]"><img src="/static/bootstrap-icons/fullscreen.svg" class="icon-btn" /></button>
        <button id="exitFullScreenButton" class="btn btn-default hidden" title="Exit Full Screen [Escape]"><img src="/static/bootstrap-icons/fullscreen-exit.svg" class="icon-btn" /></button>
    </div>
    <script nonce="${scriptNonce}">
        $('#enterFullScreenButton').click(function() { structurizr.ui.enterFullScreen('exploreTreePanel'); });
        $('#exitFullScreenButton').click(function() { structurizr.ui.exitFullScreen(); });
    </script>

    <div style="position: fixed; bottom: 10px; left: 10px;">
        <c:choose>
            <c:when test="${embed}">
                <div id="treeTitle" style="color: #aaaaaa; font-size: 13px; user-select: none; -moz-user-select: none; -khtml-user-select: none; -webkit-user-select: none; -o-user-select: none;"></div>
            </c:when>
            <c:otherwise>
                <select id="viewSelector" class="form-select"></select>
                <script nonce="${scriptNonce}">
                    $('#viewSelector').change(function() { showSelectedView(); });
                </script>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script nonce="${scriptNonce}">
    var margin = 0;

    var viewKey = decodeURIComponent('<c:out value="${view}" />');
    var view;

    const sizes = {
        "View": 20,
        "Model": 20,
        "Person": 18,
        "SoftwareSystem": 18,
        "SoftwareSystemInstance": 18,
        "Container": 14,
        "ContainerInstance": 14,
        "Component": 10,
        "DeploymentNode": 18,
        "InfrastructureNode": 14
    };

    function workspaceLoaded() {
        structurizr.ui.loadThemes(function() {
            init();
        });
    }

    function init() {
        if (viewKey.length === 0) {
            if (window.location.hash.length > 1) {
                viewKey = decodeURIComponent(window.location.hash.substring(1));
            } else {
                viewKey = structurizr.workspace.getViews()[0].key;
            }
        }

        renderTree();

        <c:choose>
        <c:when test="${embed}">
        $('#treeTitle').text(structurizr.ui.getTitleForView(view));
        </c:when>
        <c:otherwise>
        const views = structurizr.workspace.getViews();
        views.forEach(function(view) {
            if (
                view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                view.type === structurizr.constants.COMPONENT_VIEW_TYPE ||
                view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
                $('#viewSelector').append(
                    $('<option></option>').val(view.key).html(structurizr.ui.getTitleForView(view))
                );
            }
        });
        $('#viewSelector').val(viewKey);
        </c:otherwise>
        </c:choose>

        $('#brandingLogoAnchor').attr('href', '<c:out value="${urlPrefix}" />');
        progressMessage.hide();
    }

    function renderTree() {
        var treeStructure;

        view = structurizr.workspace.findViewByKey(viewKey);
        if (view !== undefined) {
            if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
                // find the base view instead
                view = structurizr.workspace.findViewByKey(view.baseViewKey);
            }
        }

        if (view !== undefined) {
            if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
                treeStructure = getDeploymentViewAsTreeStructure();
            } else {
                treeStructure = getStaticViewAsTreeStructure();
            }

            if (treeStructure) {
                graph(
                    structurizr.ui.getTitleForView(view),
                    d3.hierarchy(treeStructure)
                );
            }
        }

        setWidthAndHeight();
        $('#exploreTreePanel')[0].scrollBy(0, ($("#exploreTree").innerHeight()/2));
    }

    function graph(label, root) {
        const horizontalMargin = 50;
        const width = window.innerWidth - horizontalMargin;
        const dx = 100;
        const dy = width / 5;
        const tree = d3.tree().nodeSize([dx, dy]);
        const treeLink = d3.linkHorizontal().x(d => d.y).y(d => d.x);
        const marginLeft = 100;

        root = tree(root);

        let minX = Infinity;
        let maxX = -Infinity;
        root.each(d => {
            minX = Math.min(d.x, minX);
            maxX = Math.max(d.x, maxX);
        });

        const height = maxX - minX + dx * 2;

        const svg = d3.create("svg")
            .attr("width", (width+dy) + "px")
            .attr("height", height + "px")
            .style("overflow", "visible");

        const g = svg.append("g")
            .attr("font-size", 12)
            .attr("transform", function() {
                return "translate(" + marginLeft + "," + (dx - minX) + ")";
            })

        const link = g.append("g")
            .attr("fill", "none")
            .attr("stroke", "#555")
            .attr("stroke-opacity", 0.4)
            .attr("stroke-width", 1.5)
            .selectAll("path")
            .data(root.links())
            .join("path")
            .attr("d", treeLink);

        const node = g.append("g")
            .attr("stroke-linejoin", "round")
            .attr("stroke-width", 3)
            .selectAll("g")
            .data(root.descendants())
            .join("g")
            .attr("transform", function(d) {
                return 'translate(' + d.y + ',' + d.x +')';
            })
            .on("mouseover", showTooltipForElement)
            .on("mousemove", moveTooltip)
            .on("mouseout", hideTooltip);

        node.filter(function(d) {
            return d.data.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE || d.data.type === structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE;
        }).append("rect")
            .attr("fill", function(d) {
                return d.data.style.background;
            })
            .style("stroke-width", function(d) {
                return Math.max(2, d.data.style.strokeWidth / 2);
            })
            .attr("stroke", function(d) {
                return d.data.style.stroke;
            })
            .attr("width", function(d) {
                return sizes[d.data.type] * 2;
            })
            .attr("height", function(d) {
                return sizes[d.data.type] * 2;
            })
            .attr("x", function(d){  return -(sizes[d.data.type]);})
            .attr("y", function(d){  return -(sizes[d.data.type]);})
            .attr("rx", "3")
            .attr("ry", "3");

        node.filter(function(d) {
            return d.data.type !== structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE && d.data.type !== structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE;
        }).append("circle")
            .attr("fill", function(d) {
                return d.data.style.background;
            })
            .style("stroke-width", function(d) {
                return Math.max(2, d.data.style.strokeWidth / 2);
            })
            .attr("stroke", function(d) {
                return d.data.style.stroke;
            })
            .attr("r", function(d) {
                return sizes[d.data.type];
            });

        node.filter(function(d) {
            return d.data.style.icon !== undefined;
        }).append("image")
            .attr("width", function(d) {
                return sizes[d.data.type]*1.5;
            })
            .attr("height", function(d) {
                return sizes[d.data.type]*1.5;
            })
            .attr("x", function(d) {
                return -(sizes[d.data.type]*1.5)/2;
            })
            .attr("y", function(d) {
                return -(sizes[d.data.type]*1.5)/2;
            })
            .attr("href", function(d) {
                return d.data.style.icon;
            });

        node.append("text")
            .attr("dy", "0.31em")
            .attr("x", function(d) {
                if (d.children) {
                    return -(sizes[d.data.type] * 1.4);
                } else {
                    return (sizes[d.data.type] * 1.4);
                }
            })
            .attr("text-anchor", d => d.children ? "end" : "start")
            .text(function(d) {
                return d.data.name;
            });

        $('#exploreTree').html(svg.node());
    }

    function showTooltipForElement(event, d) {
        if (d.data.element) {
            tooltip.showTooltipForElement(d.data.element, d.data.style, event.pageX, event.pageY, true, undefined);
        }
    }

    function moveTooltip(event, d) {
        tooltip.reposition(event.pageX, event.pageY);

    }

    function hideTooltip(event, d) {
        tooltip.hide();
    }

    $('#embeddedControls').hover(
        function() {
            $('#embeddedControls').css('opacity', '1.0');
        },
        function() {
            $('#embeddedControls').css('opacity', '0.1');
        }
    );

    function setWidthAndHeight() {
        var navHeight = 0;

        <c:if test="${empty iframe}">
        if (structurizr.ui.isFullScreen()) {
            navHeight = 0;
        } else {
            navHeight = $('#topNavigation').outerHeight();
        }
        </c:if>

        var height = window.innerHeight - navHeight - margin;

        $("#exploreTree").height(height);
    }

    window.addEventListener("resize", function() {
        setWidthAndHeight();
    }, false);

    function openTreeInNewWindow() {
        window.open('<c:out value="${urlPrefix}" />/explore/tree#' + encodeURIComponent(viewKey));
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
        renderTree();
        window.location.hash = viewKey;
    }

    function getStaticViewAsTreeStructure() {
        const filter = [];

        view.elements.forEach(function(elementView) {
            var element = structurizr.workspace.findElementById(elementView.id);
            filter.push(element.id);
            while (element.parentId !== undefined) {
                if (filter.indexOf(element.parentId) === -1) {
                    filter.push(element.parentId);
                }
                element = structurizr.workspace.findElementById(element.parentId);
            }
        });

        const softwareSystems = [];

        structurizr.workspace.model.softwareSystems.forEach(function(softwareSystem) {
            if (inFilter(softwareSystem, filter)) {
                const s = {
                    name: softwareSystem.name,
                    element: softwareSystem,
                    type: structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE,
                    style: structurizr.ui.findElementStyle(softwareSystem, structurizr.ui.isDarkMode()),
                    children: []
                };
                softwareSystems.push(s);

                if (softwareSystem.containers) {
                    softwareSystem.containers.forEach(function (container) {
                        if (inFilter(container, filter)) {
                            const c = {
                                name: container.name,
                                element: container,
                                type: structurizr.constants.CONTAINER_ELEMENT_TYPE,
                                style: structurizr.ui.findElementStyle(container, structurizr.ui.isDarkMode()),
                                children: []
                            };
                            s.children.push(c);

                            if (container.components) {
                                container.components.forEach(function (component) {
                                    if (inFilter(component, filter)) {
                                        const cc = {
                                            element: component,
                                            name: component.name,
                                            type: structurizr.constants.COMPONENT_ELEMENT_TYPE,
                                            style: structurizr.ui.findElementStyle(component, structurizr.ui.isDarkMode())
                                        };
                                        c.children.push(cc);
                                    }
                                });

                                sortArrayByNameAscending(c.children);
                            }
                        }
                    });

                    sortArrayByNameAscending(s.children);
                }
            }
        });

        sortArrayByNameAscending(softwareSystems);

        return {
            name: "",
            children: softwareSystems,
            description: "",
            type: "View",
            style: {
                background: '#777777',
                stroke: '#000000'
            }
        };
    }

    function getDeploymentViewAsTreeStructure() {
        const filter = view.elements.map(function(e) { return e.id; });
        const deploymentNodes = [];

        structurizr.workspace.model.deploymentNodes.forEach(function(deploymentNode) {
            if (inFilter(deploymentNode, filter)) {
                deploymentNodes.push(getDeploymentNodeAsTreeStructure(deploymentNode, filter));
            }

        });

        sortArrayByNameAscending(deploymentNodes);

        return {
            name: "",
            children: deploymentNodes,
            description: "",
            type: "View",
            style: {
                background: '#777777',
                stroke: '#000000'
            }
        };
    }

    function inFilter(element, filter) {
        return (filter === undefined) || (filter.indexOf(element.id) > -1);
    }

    function getDeploymentNodeAsTreeStructure(deploymentNode, filter) {
        const dn = {
            name: deploymentNode.name,
            element: deploymentNode,
            type: structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE,
            style: structurizr.ui.findElementStyle(deploymentNode, structurizr.ui.isDarkMode()),
            children: []
        };

        if (deploymentNode.children) {
            deploymentNode.children.forEach(function(child) {
                if (inFilter(child, filter)) {
                    dn.children.push(getDeploymentNodeAsTreeStructure(child, filter));
                }
            });
        }

        if (deploymentNode.infrastructureNodes) {
            deploymentNode.infrastructureNodes.forEach(function(infrastructureNode) {
                if (inFilter(infrastructureNode, filter)) {
                    dn.children.push({
                        name: infrastructureNode.name,
                        element: infrastructureNode,
                        type: structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE,
                        style: structurizr.ui.findElementStyle(infrastructureNode, structurizr.ui.isDarkMode())
                    });
                }
            });
        }

        if (deploymentNode.softwareSystemInstances) {
            deploymentNode.softwareSystemInstances.forEach(function(softwareSystemInstance) {
                if (inFilter(softwareSystemInstance, filter)) {
                    dn.children.push({
                        name: softwareSystemInstance.name,
                        element: softwareSystemInstance,
                        type: structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE,
                        style: structurizr.ui.findElementStyle(softwareSystemInstance, structurizr.ui.isDarkMode())
                    });
                }
            });
        }

        if (deploymentNode.containerInstances) {
            deploymentNode.containerInstances.forEach(function(containerInstance) {
                if (inFilter(containerInstance, filter)) {
                    dn.children.push({
                        name: containerInstance.name,
                        element: containerInstance,
                        type: structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE,
                        style: structurizr.ui.findElementStyle(containerInstance, structurizr.ui.isDarkMode())
                    });
                }
            });
        }

        return dn;
    }

    function sortArrayByNameAscending(array) {
        array.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        })
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