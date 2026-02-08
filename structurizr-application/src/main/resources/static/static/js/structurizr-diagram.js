structurizr.ui.Diagram = function(id, diagramIsEditable, constructionCompleteCallback) {

    const self = this;
    const font = { name: structurizr.ui.DEFAULT_FONT_NAME };
    const gridSize = 5;
    const nameFontSizeDifferenceRatio = 1.4;
    const metadataFontSizeDifferenceRatio = 0.7;

    const darkenPercentage = -10;

    var scale = 0.5;
    var minZoomScale = 0.1;
    const DEFAULT_MAX_ZOOM_SCALE = 2;
    var maxZoomScale = DEFAULT_MAX_ZOOM_SCALE;
    const zoomSteps = 40;
    var zoomDelta = (maxZoomScale - minZoomScale) / zoomSteps;
    var pageSizeDelta = 100;

    const thumbnailWidth = 600;

    var diagramWidth = 0;
    var diagramHeight = 0;
    var margin = 400;
    var clusterPadding = 50;
    var scrollBarWidth = 0;
    var lineHeight = '1.2em';
    var diagramKey;
    var totalWidthOfKey;
    var totalHeightOfKey;
    var tooltip;
    var lasso;

    var editable = diagramIsEditable;
    var embedded = false;
    var keyboardShortcutsEnabled = true;
    var navigationEnabled = false;
    var metadataEnabled = true;
    var descriptionEnabled = true;

    var darkMode = false;
    var canvasColor = '#ffffff';
    const canvasColorDarkMode = '#111111';

    var diagramTitle;
    var diagramDescription;
    var diagramMetadata;
    var diagramTitleElement;
    var diagramDescriptionElement;
    var diagramMetadataElement;
    var diagramMetadataWidth = 0;
    var diagramMetadataHeight = 0;
    var brandingLogo;
    var elementStyleForDiagramTitle;
    var elementStyleForDiagramDescription;
    var elementStyleForDiagramMetadata;

    var enterpriseBoundary; // for backwards compatibility with older workspace definitions
    var boundariesByElementId = {};
    var groupsByName = {};
    var mapOfIdToBox = {};
    var mapOfIdToLine = {};
    var cells;
    var cellsByElementId;
    var lines;
    var linesByRelationshipId;
    var connections;
    var selectedElements = [];
    var highlightedElement = undefined;
    var highlightedLink = undefined;

    var elementStylesInUse = [];
    var elementStylesInUseMap = {};

    var relationshipStylesInUse = [];
    var relationshipStylesInUseMap = {};

    var currentX;
    var currentY;

    var animationStarted = false;
    var linesToAnimate;
    var animationSteps;
    var animationIndex;
    const animationDelay = 2000;

    var currentView;
    var currentFilter;

    var filter = {
        active: false,
        tags: [],
        perspective: undefined
    };

    var lassoStart;
    var lassoEnd;
    var dragStartPosition;
    var scrollStartPosition;

    var previousPositions = [];
    var undoStack;

    // event handlers
    var viewChangedEventHandler;
    var workspaceChangedEventHandler;
    var elementsSelectedEventHandler;
    var elementDoubleClickedHandler;
    var relationshipDoubleClickedHandler;
    var animationStartedEventHandler;
    var animationStoppedEventHandler;
    var onKeyDownEventHandler;
    var onKeyPressEventHandler;

    var imageMetadata = undefined;
    var imagePreloadAttempts = 0;
    var diagramRendered = false;

    var parentElement = $('#' + id);
    const viewportId = id + '-viewport';
    const canvasId = id + '-canvas';
    parentElement.append('<div id="' + viewportId + '" class="structurizrDiagramViewport"><div id="' + canvasId + '" class=structurizrDiagramCanvas"></div></div>');

    const viewport = $('#' + viewportId);
    const canvas = $('#' + canvasId);

    joint.config.useCSSSelectors = true;
    const graph = new joint.dia.Graph;
    const paper = new joint.dia.Paper({
        el: canvas,
        width: diagramWidth,
        height: diagramHeight,
        background: {
            color: canvasColor
        },
        model: graph,
        gridSize: gridSize,
        scale: scale,
        interactive: editable,
        defaultConnectionPoint: { name: 'bbox' },
        clickThreshold: 1,
        sorting: joint.dia.Paper.sorting.APPROX
    });

    addPaperEventHandlers();
    preloadImages();

    function getImagesToPreload() {
        var images = [];

        // start with all tags used on elements/relationships
        const tags = structurizr.workspace.getTags();

        // then add the boundary and group tags, plus the diagram icon tag
        tags.push('Boundary');
        tags.push('Boundary:Enterprise');
        tags.push('Boundary:SoftwareSystem');
        tags.push('Boundary:Container');
        tags.push('Group');
        tags.push('Workspace:Icon');
        structurizr.workspace.views.configuration.styles.elements.forEach(function(elementStyle) {
            if (elementStyle.tag.indexOf('Group:') > -1) {
                tags.push(elementStyle.tag);
            }
        })

        structurizr.ui.themes.forEach(function(theme) {
            theme.elements.forEach(function(elementStyle) {
                if (elementStyle.tag.indexOf('Group:') > -1) {
                    tags.push(elementStyle.tag);
                }
            })
        });

        tags.forEach(function(tag) {
            structurizr.ui.themes.forEach(function(theme) {
                theme.elements.forEach(function(elementStyle) {
                    if (elementStyle.tag === tag && elementStyle.icon) {
                        if (elementStyle.icon) {
                            images.push(elementStyle.icon);
                        }
                    }
                })
            });

            structurizr.workspace.views.configuration.styles.elements.forEach(function(elementStyle) {
                if (elementStyle.tag === tag && elementStyle.icon) {
                    if (elementStyle.icon) {
                        images.push(elementStyle.icon);
                    }
                }
            })
        });

        structurizr.workspace.views.imageViews.forEach(function(view) {
            if (view.content) {
                images.push(view.content);
            }

            if (view.contentLight) {
                images.push(view.contentLight);
            }

            if (view.contentDark) {
                images.push(view.contentDark);
            }
        })

        return images;
    }

    function preloadImages() {
        if (imageMetadata === undefined) {
            imageMetadata = [];

            getImagesToPreload().forEach(function(url) {
                if (getImageMetadata(url) === undefined) {
                    imageMetadata.push({
                        src: url,
                        loaded: false,
                        error: false
                    });
                }
            });

            imageMetadata.forEach(function(im) {
                const image = new Image();
                image.crossOrigin = 'anonymous';
                image.addEventListener('load', function () {
                    im.width = this.naturalWidth;
                    im.height = this.naturalHeight;
                    im.ratio = (this.naturalWidth / this.naturalHeight);
                    im.loaded = true;
                    im.error = false;

                    var canvas = document.createElement('canvas');
                    var ctx = canvas.getContext('2d');
                    canvas.height = this.naturalHeight;
                    canvas.width = this.naturalWidth;
                    ctx.drawImage(this, 0, 0);
                    im.dataURL = canvas.toDataURL(structurizr.constants.CONTENT_TYPE_IMAGE_PNG);
                });
                image.addEventListener('error', function (error) {
                    // there was an error loading the image, so ignore and continue
                    console.log('There was an error loading the image ' + im.src + ' - please check that the image exists, and that the Access-Control-Allow-Origin header is set to allow cross-origin requests.');
                    im.loaded = true;
                    im.error = true;

                    structurizr.ui.ignoredImages.push(this.src);
                });

                image.src = im.src;
            });
        } else {
            imagePreloadAttempts++;
        }

        if (imageMetadata.length > 0 && imageMetadata.filter(function(im) { return im.loaded === false; }).length > 0 && imagePreloadAttempts < 50) {
            setTimeout(preloadImages, 100);
        } else {
            var failedImages = [];

            imageMetadata.forEach(function(im) {
                if (im.loaded === false) {
                    console.log('Failed to preload ' + im.src);
                    im.error = true;
                }

                if (im.error) {
                    failedImages.push(im.src);
                }
            });

            if (failedImages.length > 0) {
                alert('The following images/icons failed to load - please see your web browser’s developer console for more details:\n\n' + failedImages.join('\n'));
            }

            if (constructionCompleteCallback) {
                setTimeout(constructionCompleteCallback, 10);
            }
        }
    }

    this.isRendered = function() {
        return diagramRendered;
    };

    this.reset = function() {
        diagramRendered = false;
    };

    this.isExportable = function() {
        return true;
    };

    this.setTooltip = function(t) {
        tooltip = t;
    };

    this.setLasso = function(l) {
        lasso = l;
    };

    function fireWorkspaceChangedEvent() {
        if (workspaceChangedEventHandler !== undefined) {
            workspaceChangedEventHandler();
        }
    }

    this.onWorkspaceChanged = function(f) {
        workspaceChangedEventHandler = f;
    };

    function fireViewChangedEvent(view) {
        if (viewChangedEventHandler !== undefined) {
            viewChangedEventHandler(view.key);
        }
    }

    this.onViewChanged = function(f) {
        viewChangedEventHandler = f;
    };

    this.changeView = function(key, callback) {
        const view = structurizr.workspace.findViewByKey(key);
        if (view === undefined) {
            console.log("A view must be specified.");
            if (callback !== undefined) {
                callback();
            }
            return;
        }

        if (currentView !== undefined && view.key === currentView.key) {
            diagramRendered = true;
            if (callback !== undefined) {
                callback();
            }

            return;
        }

        if (view.type === structurizr.constants.FILTERED_VIEW_TYPE) {
            currentFilter = view;
            currentView = structurizr.workspace.findViewByKey(view.baseViewKey);
        } else {
            currentFilter = undefined;
            currentView = view;
        }

        renderView(callback);
        fireViewChangedEvent(view);

        disableCanvasDragging();
        if (editable === false || view.automaticLayout !== undefined) {
            enableCanvasDragging();
        }
    };

    this.refresh = function() {
        renderView();
    }

    function renderView(callback) {
        var view = currentView;

        graph.stopListening();
        graph.clear();
        diagramRendered = false;

        mapOfIdToBox = {};
        cells = [];
        cellsByElementId = {};
        elementStylesInUse = [];
        elementStylesInUseMap = {};

        lines = [];
        linesByRelationshipId = {};
        connections = {};
        relationshipStylesInUse = [];
        relationshipStylesInUseMap = {};

        diagramMetadataWidth = 0;
        diagramMetadataHeight = 0;
        selectedElements = [];
        enterpriseBoundary = undefined;
        boundariesByElementId = {};
        groupsByName = {};
        linesToAnimate = undefined;
        animationSteps = undefined;
        animationStarted = false;

        undoStack = new structurizr.util.Stack();

        if (view === undefined) {
            return;
        }

        // make the diagram non-editable if auto-layout algorithm is specified
        if (view.automaticLayout !== undefined) {
            editable = false;
        } else {
            editable = diagramIsEditable;
        }

        paper.setInteractivity(function(cellView) {
            if (cellView.model.isLink()) {
                return {
                    vertexAdd: false,
                    vertexMove: true
                }
            }
            return editable;
        });

        if (tooltip) {
            tooltip.hide();
        }

        elementStyleForDiagramTitle = structurizr.ui.findElementStyle( {
            type: undefined,
            tags: 'Diagram:Title'
        }, darkMode);

        elementStyleForDiagramDescription = structurizr.ui.findElementStyle( {
            type: undefined,
            tags: 'Diagram:Description'
        }, darkMode);

        elementStyleForDiagramMetadata = structurizr.ui.findElementStyle( {
            type: undefined,
            tags: 'Diagram:Metadata'
        }, darkMode);

        createDiagramMetadata();

        if (view.type === structurizr.constants.IMAGE_VIEW_TYPE) {
            var content;

            if (structurizr.ui.isDarkMode()) {
                if (view.contentDark !== undefined) {
                    content = view.contentDark;
                } else if (view.content !== undefined) {
                    content = view.content;
                } else {
                    content = view.contentLight;
                }
            } else {
                if (view.contentLight !== undefined) {
                    content = view.contentLight;
                } else if (view.content !== undefined) {
                    content = view.content;
                } else {
                    content = view.contentDark;
                }
            }

            editable = false;
            const imageMetadata = getImageMetadata(content);

            if (imageMetadata.error) {
                content = '/static/img/image-not-available.png';
                imageMetadata.contentType = structurizr.constants.CONTENT_TYPE_IMAGE_PNG;
                imageMetadata.width = 200;
                imageMetadata.height = 100;
                imageMetadata.ratio = 2;
            } else {
                imageMetadata.contentType = view.contentType;
            }

            var imageWidth = imageMetadata.width;
            var imageHeight = imageMetadata.height;

            if (imageMetadata.contentType === structurizr.constants.CONTENT_TYPE_IMAGE_SVG) {
                // scale smaller SVGs, otherwise the diagram title becomes too large
                const minimumWidth = self.getPossibleViewportWidth() * 2;
                const minimumHeight = self.getPossibleViewportHeight() * 2;

                if (imageMetadata.ratio >= 1 && imageWidth < minimumWidth) {
                    // landscape image
                    const multiplier = minimumWidth / imageWidth;
                    imageWidth = imageWidth * multiplier;
                    imageHeight = imageHeight * multiplier;
                } else if (imageMetadata.ratio < 1 && imageHeight < minimumHeight) {
                    // portrait image
                    const multiplier = minimumHeight / imageHeight;
                    imageWidth = imageWidth * multiplier;
                    imageHeight = imageHeight * multiplier;
                }
            }

            const image = new structurizr.shapes.ImageView({
                size: {
                    width: imageWidth,
                    height: imageHeight
                },
                attrs: {
                    image: {
                        'xlink:href': content,
                        width: imageWidth,
                        height: imageHeight
                    }
                }
            });

            graph.addCell(image);
            image.toBack();

            var cellView = paper.findViewByModel(image);
            const domElement = $('#' + cellView.id);
            domElement.attr('style', 'cursor: default !important');

            view.dimensions = {
                width: imageWidth,
                height: imageHeight + (diagramMetadataHeight * 1.5)
            }

            self.setPaperSize(view);
            centreCellHorizontally(image);

            if (embedded) {
                self.zoomFitWidth();
                minZoomScale = scale;
                zoomDelta = (maxZoomScale - minZoomScale) / zoomSteps;
            } else {
                self.zoomToWidthOrHeight();
            }

            if (callback !== undefined) {
                callback();
            }

            diagramRendered = true;

            return;
        }

        self.setPaperSize(view);

        removeIllegalElements();

        var forceApplyAutomaticLayout = true;

        if (view.elements) {
            for (var i = 0; i < view.elements.length; i++) {
                forceApplyAutomaticLayout = forceApplyAutomaticLayout &&
                    (
                        (view.elements[i].x === undefined || view.elements[i].x === 0) &&
                        (view.elements[i].y === undefined || view.elements[i].y === 0)
                    );
            }

            for (var i = 0; i < view.elements.length; i++) {
                var positionX;
                var positionY;
                var element = structurizr.workspace.findElementById(view.elements[i].id);

                if (!includeElementOnDiagram(element) || element.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE) {
                    continue;
                }

                var elementStyle = structurizr.ui.findElementStyle(element, darkMode);
                registerElementStyle(elementStyle);

                var box;

                if (view.elements[i].x !== undefined) {
                    positionX = Math.floor(view.elements[i].x);
                } else {
                    positionX = Math.floor((Math.random() * 400) + 1);
                    view.elements[i].x = positionX;
                }

                if (view.elements[i].y !== undefined) {
                    positionY = Math.floor(view.elements[i].y);
                } else {
                    positionY = Math.floor((Math.random() * 400) + 1);
                    view.elements[i].y = positionY;
                }

                if (elementStyle.shape === 'Cylinder') {
                    box = createCylinder(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Bucket') {
                    box = createBucket(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Person') {
                    box = createPerson(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Robot') {
                    box = createRobot(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'RoundedBox') {
                    box = createBox(view, element, elementStyle, positionX, positionY, 20);
                } else if (elementStyle.shape === 'Folder') {
                    box = createFolder(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Circle') {
                    box = createEllipse(view, element, elementStyle, positionX, positionY, true);
                } else if (elementStyle.shape === 'Ellipse') {
                    box = createEllipse(view, element, elementStyle, positionX, positionY, false);
                } else if (elementStyle.shape === 'Hexagon') {
                    box = createHexagon(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Diamond') {
                    box = createDiamond(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Pipe') {
                    box = createPipe(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'WebBrowser') {
                    box = createWebBrowser(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Window') {
                    box = createWindow(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Terminal') {
                    box = createTerminal(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Shell') {
                    box = createShell(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'MobileDevicePortrait') {
                    box = createMobileDevicePortrait(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'MobileDeviceLandscape') {
                    box = createMobileDeviceLandscape(view, element, elementStyle, positionX, positionY);
                } else if (elementStyle.shape === 'Component') {
                    box = createComponent(view, element, elementStyle, positionX, positionY);
                } else {
                    box = createBox(view, element, elementStyle, positionX, positionY, 1);
                }

                cells.push(box);
                cellsByElementId[element.id] = box;

                box.elementInView = view.elements[i];
                box.positionCalculated = false;

                box.on('change:position', function(cell, newPosition, opt) {
                    if (opt.translateBy === undefined) {
                        // cell has moved programmatically
                        cell.elementInView.x = newPosition.x;
                        cell.elementInView.y = newPosition.y;
                    } else {
                        const translatedByCell = graph.getCell(opt.translateBy);
                        if (translatedByCell.attributes.type === 'structurizr.boundary' || translatedByCell.attributes.type === 'structurizr.deploymentNode') {
                            // a boundary/group/deployment node has been dragged, and moved this element
                            cell.elementInView.x = newPosition.x;
                            cell.elementInView.y = newPosition.y;
                        } else {
                            // an element has been dragged
                            var cellViewMoved = paper.findViewByModel(cell);
                            if (cellViewMoved.selected === true && selectedElements.length > 1) {
                                const dx = newPosition.x - cell.elementInView.x;
                                const dy = newPosition.y - cell.elementInView.y;

                                selectedElements.forEach(function (cellView) {
                                    if (cellView !== cellViewMoved) {
                                        moveElement(cellView.model, dx, dy);
                                    }
                                });

                                moveLinksBetweenSelectedElements(dx, dy);
                            }

                            cell.elementInView.x = newPosition.x;
                            cell.elementInView.y = newPosition.y;
                        }
                    }

                    repositionParentCells(cell);

                    fireWorkspaceChangedEvent();
                });

                if (includeGroup(element, view, currentFilter) === true) {
                    if (element.group !== undefined) {
                        var scope = element.parentId;
                        if (scope === undefined) {
                            scope = element.location === 'Internal' ? 'Internal' : 'External';
                        }
                        const group = findOrCreateGroup(element.group, scope);
                        group.embed(box);
                        box.toFront();
                    }
                }

                // enterprise boundaries need to be drawn for: system landscape, system context, and (high-level) dynamic diagrams
                // - the "enterprise" concept has been removed, so this is only here to support older workspaces
                if (view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE || view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE || (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE && view.elementId === undefined)) {
                    var includeEnterpriseBoundary = (view.enterpriseBoundaryVisible === true);
                    if (view.properties && view.properties['structurizr.enterpriseBoundary']) {
                        includeEnterpriseBoundary = (view.properties['structurizr.enterpriseBoundary'] === 'true');
                    }
                    if (element.location && element.location === 'Internal' && includeEnterpriseBoundary) {
                        if (!enterpriseBoundary) {
                            var enterprise = structurizr.workspace.model.enterprise;
                            var boundaryName = (enterprise && enterprise.name) ? enterprise.name : 'Enterprise';

                            enterpriseBoundary = createBoundary(boundaryName, structurizr.ui.getMetadataForElement({ type: 'Enterprise' }), 'Enterprise');
                        }

                        if (element.group !== undefined) {
                            const rootGroup = findRootGroup(element.group, 'Internal');
                            if (rootGroup) {
                                enterpriseBoundary.embed(rootGroup);
                            } else {
                                enterpriseBoundary.embed(box);
                            }
                        } else {
                            enterpriseBoundary.embed(box);
                        }
                    }
                }

                if (view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE && element.id === view.softwareSystemId) {
                    if (!view.elements[i].x) {
                        centreCell(box);
                    }
                }

                if (view.type === structurizr.constants.CONTAINER_VIEW_TYPE && element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
                    // container on a container diagram - add a boundary to represent the parent software system
                    addElementToBoundary(element, box, false);
                }

                if (view.type === structurizr.constants.COMPONENT_VIEW_TYPE && element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE) {
                    // component on a component diagram - add a boundary to represent the parent container
                    addElementToBoundary(element, box, true);
                }

                if (view.type === structurizr.constants.COMPONENT_VIEW_TYPE && element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
                    // container on a component diagram - add a boundary to represent the parent software system
                    addElementToBoundary(element, box, true);
                }

                if (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
                    if (
                        element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE ||
                        element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE
                    ) {
                        // dynamic view with software system scope and element is a container or
                        // dynamic view with container scope and element is a component
                        //
                        // in both cases, add a boundary to represent the scoped element
                        addElementToBoundary(element, box, true);
                    }
                }

                var cellView = paper.findViewByModel(box);
                const domElement = $('#' + cellView.id);

                if (editable === true) {
                    domElement.attr('style', 'cursor: move !important');
                } else {
                    domElement.attr('style', 'cursor: default !important');
                }

                // add some functions called by the double-click handler
                if (navigationEnabled) {
                    addDoubleClickHandlerForElement(cellView, element);
                }
            }
        }

        if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE || view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
            var unusedDeploymentNodeCells = [];

            // this first loop creates deployment nodes, nesting any software system/container instances and infrastructure nodes that have already been created
            if (view.elements) {
                view.elements.forEach(function (elementView) {
                    var element = structurizr.workspace.findElementById(elementView.id);

                    if (element.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE) {
                        var deploymentNodeCell = createDeploymentNode(element);
                        deploymentNodeCell.elementInView = element;
                        deploymentNodeCell.positionCalculated = true;
                        unusedDeploymentNodeCells.push(deploymentNodeCell);

                        cellsByElementId[element.id] = deploymentNodeCell;

                        if (includeGroup(element, view, currentFilter) === true) {
                            if (element.group !== undefined) {
                                var scope = element.parentId;
                                if (scope === undefined) {
                                    scope = element.environment;
                                }
                                const group = findOrCreateGroup(element.group, scope);
                                group.embed(deploymentNodeCell);
                            }
                        }

                        if (element.softwareSystemInstances && element.softwareSystemInstances.length > 0) {
                            element.softwareSystemInstances.forEach(function(softwareSystemInstance) {
                                // find the software system on the diagram
                                var softwareSystemBox = cellsByElementId[softwareSystemInstance.id];
                                if (softwareSystemBox !== undefined) {
                                    if (softwareSystemInstance.group !== undefined) {
                                        const rootGroup = findRootGroup(softwareSystemInstance.group, softwareSystemInstance.parentId);
                                        if (rootGroup) {
                                            deploymentNodeCell.embed(rootGroup);
                                        } else {
                                            deploymentNodeCell.embed(softwareSystemBox);
                                        }
                                    } else {
                                        deploymentNodeCell.embed(softwareSystemBox);
                                    }
                                }
                            });
                        }

                        if (element.containerInstances && element.containerInstances.length > 0) {
                            element.containerInstances.forEach(function(containerInstance) {
                                // find the container on the diagram
                                var containerBox = cellsByElementId[containerInstance.id];
                                if (containerBox !== undefined) {
                                    if (containerInstance.group !== undefined) {
                                        const rootGroup = findRootGroup(containerInstance.group, containerInstance.parentId);
                                        if (rootGroup) {
                                            deploymentNodeCell.embed(rootGroup);
                                        } else {
                                            deploymentNodeCell.embed(containerBox);
                                        }
                                    } else {
                                        deploymentNodeCell.embed(containerBox);
                                    }
                                }
                            });
                        }

                        if (element.infrastructureNodes && element.infrastructureNodes.length > 0) {
                            element.infrastructureNodes.forEach(function(infrastructureNode) {
                                // find the infrastructure node on the diagram
                                var infrastructureBox = cellsByElementId[infrastructureNode.id];
                                if (infrastructureBox !== undefined) {
                                    if (infrastructureNode.group !== undefined) {
                                        const rootGroup = findRootGroup(infrastructureNode.group, infrastructureNode.parentId);
                                        if (rootGroup) {
                                            deploymentNodeCell.embed(rootGroup);
                                        } else {
                                            deploymentNodeCell.embed(infrastructureBox);
                                        }
                                    } else {
                                        deploymentNodeCell.embed(infrastructureBox);
                                    }
                                }
                            });
                        }
                    }
                });

                // this second loop ensures that all deployment nodes are correctly embedded (because deployment nodes are created out of order)
                view.elements.forEach(function (elementView) {
                    var element = structurizr.workspace.findElementById(elementView.id);
                    var cell = cellsByElementId[element.id];

                    if (element.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE) {
                        if (element.parentId !== undefined) {
                            var parentBox = cellsByElementId[element.parentId];

                            if (element.group !== undefined) {
                                var scope = element.parentId;
                                if (scope === undefined) {
                                    scope = element.environment;
                                }
                                const rootGroup = findRootGroup(element.group, scope);
                                if (rootGroup) {
                                    parentBox.embed(rootGroup);
                                } else {
                                    parentBox.embed(cell);
                                }
                            } else {
                                parentBox.embed(cell);
                            }
                        }
                    }
                });

                // and this third loop ensures that empty deployment nodes are identified and removed from the diagram
                view.elements.forEach(function(elementView) {
                    var element = structurizr.workspace.findElementById(elementView.id);
                    var cell = cellsByElementId[element.id];

                    if (cell !== undefined) {
                        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE || element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE || element.type === structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE) {
                            var parentId = cell.get('parent');
                            while (parentId) {
                                var parentCell = graph.getCell(parentId);

                                var index = unusedDeploymentNodeCells.indexOf(parentCell);
                                if (index > -1) {
                                    unusedDeploymentNodeCells.splice(index, 1);
                                }

                                parentId = parentCell.get('parent');
                            }
                        }
                    }
                });

                unusedDeploymentNodeCells.forEach(function(deploymentNodeCell) {
                    deploymentNodeCell.remove();
                    delete cellsByElementId[deploymentNodeCell.elementInView.id];
                    delete mapOfIdToBox[deploymentNodeCell.elementInView.id]
                });
            }
        }

        var relationships = view.relationships;
        if (relationships === undefined) {
            relationships = [];
        }

        for (var i = 0; i < relationships.length; i++) {
            const relationshipView = relationships[i];

            var line = createArrow(relationshipView);
            if (line !== undefined) {
                lines.push(line);
                linesByRelationshipId[relationshipView.id] = line;

                const relationship = structurizr.workspace.findRelationshipById(relationshipView.id);

                const sourceElementId = relationship.sourceId;
                var connectionsFromElement = connections[sourceElementId];
                if (connectionsFromElement === undefined) {
                    connectionsFromElement = [];
                }
                connectionsFromElement.push(paper.findViewByModel(line));
                connections[sourceElementId] = connectionsFromElement;

                const destinationElementId = relationship.destinationId;
                var connectionsToElement = connections[destinationElementId];
                if (connectionsToElement === undefined) {
                    connectionsToElement = [];
                }
                connectionsToElement.push(paper.findViewByModel(line));
                connections[destinationElementId] = connectionsToElement;
            }
        }

        if (!editable) {
            $('.connection-wrap').css(
                {
                    'pointer-events': 'visiblePainted',
                    'cursor': 'auto'
                }
            );
            $('.marker-vertices').css('display', 'none');
        }

        diagramKey = createDiagramKey();

        repositionAllParentCells();

        if (descriptionEnabled) {
            showDescription();
        } else {
            hideDescription();
        }

        if (metadataEnabled) {
            showMetadata();
        } else {
            hideMetadata();
        }

        // ensure all elements are stacked properly, front to back
        graph.getElements().forEach(function(element) {
            if (element.get('parent') === undefined) {
                element.toFront();
                const embeddedCells = element.getEmbeddedCells({ deep: true, breadthFirst: true});
                embeddedCells.forEach(function(embeddedCell) {
                    embeddedCell.toFront();
                });
            }
        });

        if (embedded) {
            self.zoomFitWidth();
            minZoomScale = scale;
            zoomDelta = (maxZoomScale - minZoomScale) / zoomSteps;
        } else {
            maxZoomScale = DEFAULT_MAX_ZOOM_SCALE;
            self.zoomToWidthOrHeight();
        }

        runFilter();

        // adjust any overlapping vertices, and bring all relationships to the front
        lines.forEach(function(line) {
            try {
                //adjustVertices(graph, line);
                line.toFront();
            } catch (e) {
                console.log(e);
            }
        });

        if (view.automaticLayout) {
            if (view.automaticLayout) {
                structurizr.diagram.applyAutomaticLayout(
                    view.automaticLayout.rankDirection,
                    view.automaticLayout.rankSeparation,
                    view.automaticLayout.nodeSeparation,
                    view.automaticLayout.edgeSeparation,
                    view.automaticLayout.vertices
                );
            }
        } else if (forceApplyAutomaticLayout) {
            structurizr.diagram.applyAutomaticLayout(
                structurizr.ui.DEFAULT_AUTOLAYOUT_RANK_DIRECTION,
                structurizr.ui.DEFAULT_AUTOLAYOUT_RANK_SEPARATION,
                structurizr.ui.DEFAULT_AUTOLAYOUT_NODE_SEPARATION,
                structurizr.ui.DEFAULT_AUTOLAYOUT_EDGE_SEPARATION,
                structurizr.ui.DEFAULT_AUTOLAYOUT_VERTICES
            );
        }

        if (callback !== undefined) {
            callback();
        }

        diagramRendered = true;
    }

    function repositionAllParentCells() {
        // ensure that all elements are repositioned properly (e.g. groups and deployment nodes are made large enough to fit their content)
        currentView.elements.forEach(function(elementView) {
            var element = structurizr.workspace.findElementById(elementView.id);
            var cell = cellsByElementId[element.id];

            if (
                element.type === structurizr.constants.CUSTOM_ELEMENT_TYPE ||
                element.type === structurizr.constants.PERSON_ELEMENT_TYPE ||
                element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE ||
                element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE ||
                element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE ||
                element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE ||
                element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE ||
                element.type === structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE
            ) {
                repositionParentCells(cell);
            }
        });
    }

    function repositionParentCells(cell) {
        if (cell) {
            var parentId = cell.get('parent');
            while (parentId) {
                const parentCell = graph.getCell(parentId);
                reposition(parentCell);

                parentId = parentCell.get('parent');
            }
        }
    }

    function includeGroup(element, view, filter) {
        // for rendering groups, we only want to do this as follows:
        // - system landscape diagram: people, software systems, custom elements
        // - system context diagram: people, software systems, custom elements
        // - container diagram: containers
        // - component diagram: components
        // - dynamic diagram: depends on scope
        // - deployment diagram: deployment nodes, infrastructure nodes, software system instances, container instances
        var renderGroupForElement = false;

        // first check the filter
        if (filter && filter.properties && filter.properties['structurizr.groups'] === 'false') {
            return false;
        }

        // have groups been forced off?
        if (view.properties && view.properties['structurizr.groups'] === 'false') {
            return false;
        }

        if (view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE || view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE) {
            renderGroupForElement = (element.type === structurizr.constants.PERSON_ELEMENT_TYPE || element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE || element.type === structurizr.constants.CUSTOM_ELEMENT_TYPE);
        } else if (view.type === structurizr.constants.CONTAINER_VIEW_TYPE) {
            renderGroupForElement = element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE;
        } else if (view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {
            renderGroupForElement = element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE;
        } else if (view.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
            // find the type of the scoped element
            if (view.elementId === undefined) {
                renderGroupForElement = (element.type === structurizr.constants.PERSON_ELEMENT_TYPE || element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE || element.type === structurizr.constants.CUSTOM_ELEMENT_TYPE);
            } else {
                var scopedElement = structurizr.workspace.findElementById(view.elementId);
                if (scopedElement.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE) {
                    renderGroupForElement = element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE;
                } else if (scopedElement.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
                    renderGroupForElement = element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE;
                }
            }
        } else if (view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
            renderGroupForElement = (
                element.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE ||
                element.type === structurizr.constants.INFRASTRUCTURE_NODE_ELEMENT_TYPE ||
                element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE ||
                element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE
            );
        }

        return renderGroupForElement;
    }

    function addElementToBoundary(element, cell, includeParentBoundary) {
        var boundary = boundariesByElementId[element.parentId];
        if (boundary === undefined) {
            var boundaryElement = structurizr.workspace.findElementById(element.parentId);
            if (boundaryElement) {
                boundary = createBoundary(boundaryElement.name, structurizr.ui.getMetadataForElement(boundaryElement), undefined, boundaryElement);
                boundary.elementInView = boundaryElement;
                boundariesByElementId[element.parentId] = boundary;
            }

            if (includeParentBoundary && boundaryElement.parentId) {
                var parentBoundary = boundariesByElementId[boundaryElement.parentId];
                if (parentBoundary === undefined) {
                    var parentBoundaryElement = structurizr.workspace.findElementById(boundaryElement.parentId);
                    if (parentBoundaryElement) {
                        parentBoundary = createBoundary(parentBoundaryElement.name, structurizr.ui.getMetadataForElement(parentBoundaryElement), undefined, parentBoundaryElement);
                        parentBoundary.elementInView = parentBoundaryElement;
                        boundariesByElementId[boundaryElement.parentId] = parentBoundary;
                    }
                }

                parentBoundary.embed(boundary);
            }
        }

        if (element.group !== undefined) {
            const rootGroup = findRootGroup(element.group, element.parentId);
            if (rootGroup) {
                boundary.embed(rootGroup);
            } else {
                boundary.embed(cell);
            }
        } else {
            boundary.embed(cell);
        }

        //boundary.toFront({ deep: true });
    }

    function findRootGroup(name, scope) {
        if (useNestedGroups()) {
            const separator = getGroupSeparator();

            if (name.indexOf(separator) > -1) {
                return findGroup(name.substring(0, name.indexOf(separator)), scope);
            } else {
                return findGroup(name, scope);
            }
        } else {
            return findGroup(name, scope);
        }
    }

    function findGroup(name, scope) {
        if (scope === undefined) {
            scope = ""
        }
        const identifier = scope + "_" + name;

        return groupsByName[identifier];
    }

    function registerGroup(name, scope, boundary) {
        if (scope === undefined) {
            scope = ""
        }
        const identifier = scope + "_" + name;

        groupsByName[identifier] = boundary;
    }

    function getGroupSeparator() {
        return structurizr.workspace.model.properties['structurizr.groupSeparator'];
    }

    function useNestedGroups() {
        return getGroupSeparator() !== undefined;
    }

    function findOrCreateGroup(name, scope) {
        if (useNestedGroups()) {
            const separator = getGroupSeparator();
            var group = findGroup(name, scope);
            if (group === undefined) {
                if (name.indexOf(separator) > -1) {
                    var parentGroupName = name.substring(0, name.lastIndexOf(separator));
                    var groupName = name.substring(name.lastIndexOf(separator) + separator.length);
                    var parentGroup = findOrCreateGroup(parentGroupName, scope);

                    group = createBoundaryForGroup(name);
                    parentGroup.embed(group);
                    group._name = groupName;
                    registerGroup(name, scope, group);
                } else {
                    group = createBoundaryForGroup(name);
                    group._name = name;
                    registerGroup(name, scope, group);
                }
            }

            return group;
        } else {
            var group = findGroup(name, scope);
            if (group === undefined) {
                group = createBoundaryForGroup(name);
                group._name = name;
                registerGroup(name, scope, group);
            }

            return group;
        }
    }

    function addDoubleClickHandlerForElement(cellView, element) {
        var domElement = $('#' + cellView.id);
        var views = [];
        var documentation = false;
        var decisions = false;
        var url = false;

        url = element.url !== undefined;
        if (element.properties) {
            Object.keys(element.properties).forEach(function(name) {
                const value = element.properties[name];
                if (value.indexOf('http://') === 0 || value.indexOf('https://') === 0) {
                    url = true;
                }
            });
        }

        const elementDoubleClicked = element;

        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE) {
            element = structurizr.workspace.findElementById(element.softwareSystemId);
        } else if (element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE) {
            element = structurizr.workspace.findElementById(element.containerId);
        }

        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE) {
            if (currentView.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE || currentView.softwareSystemId !== element.id) {
                views = structurizr.workspace.findSystemContextViewsForSoftwareSystem(element.id).concat(structurizr.workspace.findContainerViewsForSoftwareSystem(element.id));
            } else if (currentView.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE) {
                views = structurizr.workspace.findContainerViewsForSoftwareSystem(element.id);
            }
        } else if (element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
            views = structurizr.workspace.findComponentViewsForContainer(element.id);
        }

        views = views.concat(structurizr.workspace.findImageViewsForElement(element.id));

        documentation = (element.documentation && element.documentation.sections && element.documentation.sections.length > 0);
        decisions = (element.documentation && element.documentation.decisions && element.documentation.decisions.length > 0);

        if (url !== undefined) {
            domElement.attr('style', 'cursor: pointer !important');
        }

        var translateX = 5;
        var translateXDelta = 25;
        if (views.length > 0) {
            const svg =
                '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">' +
                '<path fill-rule="evenodd" d="M6.5 12a5.5 5.5 0 1 0 0-11 5.5 5.5 0 0 0 0 11zM13 6.5a6.5 6.5 0 1 1-13 0 6.5 6.5 0 0 1 13 0z"/>' +
                '<path d="M10.344 11.742c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1 6.538 6.538 0 0 1-1.398 1.4z"/>' +
                '<path fill-rule="evenodd" d="M6.5 3a.5.5 0 0 1 .5.5V6h2.5a.5.5 0 0 1 0 1H7v2.5a.5.5 0 0 1-1 0V7H3.5a.5.5 0 0 1 0-1H6V3.5a.5.5 0 0 1 .5-.5z"/>' +
                '</svg>';

            $('#' + cellView.id + " .structurizrZoom").html(svg);
            $('#' + cellView.id + " .structurizrZoom").attr('transform', 'translate(' + translateX + ' 0)');
            translateX += translateXDelta;
        }

        if (documentation) {
            const svg =
                '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">' +
                '<path d="M1 2.828c.885-.37 2.154-.769 3.388-.893 1.33-.134 2.458.063 3.112.752v9.746c-.935-.53-2.12-.603-3.213-.493-1.18.12-2.37.461-3.287.811V2.828zm7.5-.141c.654-.689 1.782-.886 3.112-.752 1.234.124 2.503.523 3.388.893v9.923c-.918-.35-2.107-.692-3.287-.81-1.094-.111-2.278-.039-3.213.492V2.687zM8 1.783C7.015.936 5.587.81 4.287.94c-1.514.153-3.042.672-3.994 1.105A.5.5 0 0 0 0 2.5v11a.5.5 0 0 0 .707.455c.882-.4 2.303-.881 3.68-1.02 1.409-.142 2.59.087 3.223.877a.5.5 0 0 0 .78 0c.633-.79 1.814-1.019 3.222-.877 1.378.139 2.8.62 3.681 1.02A.5.5 0 0 0 16 13.5v-11a.5.5 0 0 0-.293-.455c-.952-.433-2.48-.952-3.994-1.105C10.413.809 8.985.936 8 1.783z"/>' +
                '</svg>';

            $('#' + cellView.id + " .structurizrDocumentation").html(svg);
            $('#' + cellView.id + " .structurizrDocumentation").attr('transform', 'translate(' + translateX + ' 0)');
            translateX += translateXDelta;
        }

        if (decisions) {
            const svg =
                '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">' +
                '<path d="M5 10.5a.5.5 0 0 1 .5-.5h2a.5.5 0 0 1 0 1h-2a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5z"/>' +
                '<path d="M3 0h10a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2v-1h1v1a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H3a1 1 0 0 0-1 1v1H1V2a2 2 0 0 1 2-2z"/>' +
                '<path d="M1 5v-.5a.5.5 0 0 1 1 0V5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0V8h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0v.5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1z"/>' +
                '</svg>';

            $('#' + cellView.id + " .structurizrDecisions").html(svg);
            $('#' + cellView.id + " .structurizrDecisions").attr('transform', 'translate(' + translateX + ' 0)');
            translateX += translateXDelta;
        }

        if (url) {
            const svg =
                '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">' +
                '<path d="M6.354 5.5H4a3 3 0 0 0 0 6h3a3 3 0 0 0 2.83-4H9c-.086 0-.17.01-.25.031A2 2 0 0 1 7 10.5H4a2 2 0 1 1 0-4h1.535c.218-.376.495-.714.82-1z"/>' +
                '<path d="M9 5.5a3 3 0 0 0-2.83 4h1.098A2 2 0 0 1 9 6.5h3a2 2 0 1 1 0 4h-1.535a4.02 4.02 0 0 1-.82 1H12a3 3 0 1 0 0-6H9z"/>' +
                '</svg>';

            $('#' + cellView.id + " .structurizrLink").html(svg);
            $('#' + cellView.id + " .structurizrLink").attr('transform', 'translate(' + translateX + ' 0)');
            translateX += translateXDelta;
        }

        if (views.length > 0 || documentation || decisions) {
            domElement.attr('style', 'cursor: zoom-in !important');
        }

        const width = cellView.model._computedStyle.width;
        const navigationRefX = (((width - translateX) / 2) / width);
        cellView.model.attr('.structurizrNavigation/ref-x', navigationRefX);

        domElement.dblclick(function(event) {
            if (elementDoubleClickedHandler !== undefined) {
                elementDoubleClickedHandler(event, elementDoubleClicked.id);
            }
        });
    }

    function relationshipHasUrl(relationship) {
        var result = (relationship.url !== undefined);

        if (!result) {
            if (relationship.properties) {
                Object.keys(relationship.properties).forEach(function(name) {
                    const value = relationship.properties[name];
                    if (value.indexOf('http://') === 0 || value.indexOf('https://') === 0) {
                        result = true;
                    }
                });
            }
        }

        if (result === false) {
            if (relationship.linkedRelationshipId) {
                return relationshipHasUrl(structurizr.workspace.findRelationshipById(relationship.linkedRelationshipId));
            }
        }

        return result;
    }

    function addDoubleClickHandlerForRelationship(linkView, relationship) {
        if (relationshipHasUrl(relationship)) {
            const domElement = $('#' + linkView.id);

            const svg =
                '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="structurizrNavigation" viewBox="0 0 16 16">' +
                '<path d="M6.354 5.5H4a3 3 0 0 0 0 6h3a3 3 0 0 0 2.83-4H9c-.086 0-.17.01-.25.031A2 2 0 0 1 7 10.5H4a2 2 0 1 1 0-4h1.535c.218-.376.495-.714.82-1z"/>' +
                '<path d="M9 5.5a3 3 0 0 0-2.83 4h1.098A2 2 0 0 1 9 6.5h3a2 2 0 1 1 0 4h-1.535a4.02 4.02 0 0 1-.82 1H12a3 3 0 1 0 0-6H9z"/>' +
                '</svg>';

            $('#' + linkView.id + " .structurizrNavigation").parent().css('color', linkView.model._computedStyle.color );
            $('#' + linkView.id + " .structurizrNavigation").parent().html(svg);

            $('#' + linkView.id + ' .connection-wrap').css('cursor', 'pointer');
            $('#' + linkView.id + ' .label').css('cursor', 'pointer');

            domElement.dblclick(function(event) {
                if (relationshipDoubleClickedHandler !== undefined) {
                    relationshipDoubleClickedHandler(event, relationship.id);
                }
            });
        } else {
            $('#' + linkView.id + ' .connection-wrap').css('cursor', 'default');
            $('#' + linkView.id + ' .label').css('cursor', 'default');
        }
    }

    this.setPerspective = function(perspective) {
        filter.perspective = perspective;
    };

    this.showPerspective = function(perspective) {
        filter.perspective = perspective;
        runFilter();
    };

    this.hasPerspective = function() {
        return filter.perspective !== undefined;
    }

    this.getPerspective = function() {
        return filter.perspective;
    }

    this.clearPerspective = function() {
        filter.perspective = undefined;
        runFilter();
    };

    function elementHasPerspective(element) {
        return getPerspectiveForElement(element) !== undefined;
    }

    function getPerspectiveForElement(element) {
        var p = undefined;

        if (element.perspectives) {
            element.perspectives.forEach(function(perspective) {
                if (perspective.name === filter.perspective) {
                    p = perspective;
                }
            });
        }

        if (p === undefined) {
            if (element.type === structurizr.constants.SOFTWARE_SYSTEM_INSTANCE_ELEMENT_TYPE) {
                var softwareSystem = structurizr.workspace.findElementById(element.softwareSystemId);
                if (softwareSystem.perspectives) {
                    softwareSystem.perspectives.forEach(function (perspective) {
                        if (perspective.name === filter.perspective) {
                            p = perspective;
                        }
                    });
                }
            } else if (element.type === structurizr.constants.CONTAINER_INSTANCE_ELEMENT_TYPE) {
                var container = structurizr.workspace.findElementById(element.containerId);
                if (container.perspectives) {
                    container.perspectives.forEach(function (perspective) {
                        if (perspective.name === filter.perspective) {
                            p = perspective;
                        }
                    });
                }
            }
        }

        return p;
    }

    function relationshipHasPerspective(relationship) {
        return getPerspectiveForRelationship(relationship) !== undefined;
    }

    function getPerspectiveForRelationship(relationship) {
        var p;

        if (relationship.perspectives) {
            relationship.perspectives.forEach(function(perspective) {
                if (perspective.name === filter.perspective) {
                    p = perspective;
                }
            });
        }

        if (p === false) {
            if (relationship.linkedRelationshipId) {
                p = getPerspectiveForRelationship(structurizr.workspace.findRelationshipById(relationship.linkedRelationshipId));
            }
        }

        return p;
    }

    this.setFilter = function(f) {
        filter = f;
        runFilter();
    }

    this.getFilter = function() {
        return filter;
    }

    this.filterOff = function() {
        filter.active = false;
        runFilter();
    }

    function elementMatchesFilter(element) {
        if (filter.active) {
            const tags = structurizr.workspace.getAllTagsForElement(element);
            var hasTags = false;
            filter.tags.forEach(function(tag) {
                hasTags = (hasTags || tags.indexOf(tag) > -1);
            });

            const hasPerspective = (filter.perspective === undefined || getPerspectiveForElement(element));

            return hasTags && hasPerspective;
        } else {
            return true;
        }
    }

    function relationshipMatchesFilter(relationship) {
        if (filter.active) {
            const tags = structurizr.workspace.getAllTagsForRelationship(relationship);
            var hasTags = false;
            filter.tags.forEach(function(tag) {
                hasTags = (hasTags || tags.indexOf(tag) > -1);
            });

            const hasPerspective = (filter.perspective === undefined || relationshipHasPerspective(relationship));

            return hasTags && hasPerspective;
        } else {
            return true;
        }
    }

    function runFilter() {
        var itemsHidden = false;
        const hiddenOpacity = '0.1';

        var elementStylesForPerspective = [];
        var relationshipStylesForPerspective = [];

        var refresh = false;

        if (filter.perspective) {
            elementStylesForPerspective = structurizr.ui.getElementStylesForPerspective(filter.perspective, darkMode);
            relationshipStylesForPerspective = structurizr.ui.getRelationshipStylesForPerspective(filter.perspective, darkMode);
        }

        const elements = [];
        Object.keys(cellsByElementId).forEach(function(elementId) {
            const cell = cellsByElementId[elementId];
            elements.push(structurizr.workspace.findElementById(cell.elementInView.id));
        });

        elements.forEach(function(element) {
            if (elementMatchesFilter(element)) {
                const cell = cellsByElementId[element.id];
                changeColourOfCell(cell, cell._computedStyle.background, cell._computedStyle.color, cell._computedStyle.stroke);

                if (filter.perspective) {
                    const perspective = getPerspectiveForElement(element);

                    if (perspective !== undefined) {
                        showElement(element.id);

                        if (perspective.url && perspective.url.length > 0) {
                            refresh = true;
                            runDynamicPerspective(perspective, function() {
                                applyElementStyleForPerspective(cell, perspective, elementStylesForPerspective);
                            });
                        } else {
                            applyElementStyleForPerspective(cell, perspective, elementStylesForPerspective);
                        }
                    } else {
                        hideElement(element.id, hiddenOpacity);
                        itemsHidden = true;
                    }
                } else {
                    showElement(element.id);
                }
            } else {
                hideElement(element.id, hiddenOpacity);
                itemsHidden = true;
            }
        });

        const relationships = [];
        lines.forEach(function (line) {
            relationships.push(structurizr.workspace.findRelationshipById(line.relationshipInView.id));
        });

        relationships.forEach(function(relationship) {
            if (relationshipMatchesFilter(relationship)) {
                const line = linesByRelationshipId[relationship.id];
                changeColourOfLine(line, line._computedStyle.color);

                if (filter.perspective) {
                    const perspective = getPerspectiveForRelationship(relationship);

                    if (perspective !== undefined) {
                        showRelationship(relationship.id);

                        if (perspective.url && perspective.url.length > 0) {
                            refresh = true;
                            runDynamicPerspective(perspective, function() {
                                applyRelationshipStyleForPerspective(line, perspective, relationshipStylesForPerspective);
                            });
                        } else {
                            applyRelationshipStyleForPerspective(line, perspective, relationshipStylesForPerspective);
                        }
                    } else {
                        hideRelationship(relationship.id, hiddenOpacity);
                        itemsHidden = true;
                    }
                } else {
                    showRelationship(relationship.id);
                }
            } else {
                hideRelationship(relationship.id, hiddenOpacity);
                itemsHidden = true;
            }
        });

        if (itemsHidden) {
            hideBoundaries(hiddenOpacity);
        } else {
            showBoundaries();
        }

        if (refresh) {
            const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
            const interval = getViewOrViewSetProperty(viewOrFilter, 'structurizr.perspective.interval', '60000');

            setTimeout(function () {
                runFilter();
            }, interval);
        }
    }

    function runDynamicPerspective(perspective, callback) {
        const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
        const timeout = getViewOrViewSetProperty(viewOrFilter, 'structurizr.perspective.timeout', '10000');

        try {
            $.ajax({
                url: perspective.url,
                dataType: 'text',
                timeout: timeout
            })
            .done(function(data, textStatus, jqXHR) {
                perspective.value = data;

                // default to HTTP status if response body is empty
                if (!perspective.value || perspective.value.length === 0) {
                    perspective.value = jqXHR.status;
                }
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(perspective.url + ': ' + textStatus + ' (HTTP status code=' + jqXHR.status + ')');
                perspective.value = '' + jqXHR.status;
            })
            .always(function () {
                try {
                    callback();
                } catch (err) {
                    console.log(err);
                }
            });
        } catch (err) {
            console.log(err);
        }
    }

    function findStyleForPerspective(styles, perspective) {
        var styleForPerspective;

        // find the basic perspective style first
        styles.forEach(function(style) {
            if (style.tag.indexOf('[') === -1) {
                styleForPerspective = style;
            }
        });

        // find a style for [value==x]
        styles.forEach(function(style) {
            if (style.tag.indexOf('[value') > 0) {
                const expression = style.tag.substring(style.tag.indexOf('[')+1, style.tag.length-1);

                if (expression.indexOf('value==') === 0) {
                    const testValue = expression.substring('value=='.length);

                    if (perspective.value === testValue) {
                        styleForPerspective = style;
                    }
                }
            }
        });

        return styleForPerspective;
    }

    function applyElementStyleForPerspective(cell, perspective, stylesForPerspective) {
        const style = findStyleForPerspective(stylesForPerspective, perspective);

        if (style !== undefined) {
            var background = style.background;
            var color = style.color;
            var stroke = style.stroke;

            if (background === undefined) {
                background = cell._computedStyle.background;
            }

            if (color === undefined) {
                color = cell._computedStyle.color;
            }

            if (stroke === undefined) {
                stroke = structurizr.util.shadeColor(background, darkenPercentage, darkMode);
            }

            cell._perspectiveStyle = { background: background, color: color, stroke: stroke };
            changeColourOfCell(cell, background, color, stroke);
        } else {
            cell._perspectiveStyle = {
                background: cell._computedStyle.background,
                color: cell._computedStyle.color,
                stroke: cell._computedStyle.stroke
            };
        }
    }

    function applyRelationshipStyleForPerspective(line, perspective, stylesForPerspective) {
        const style = findStyleForPerspective(stylesForPerspective, perspective);

        if (style !== undefined) {
            var color = style.color;

            if (color === undefined) {
                color = line._computedStyle.color;
            }

            line._perspectiveStyle = { color: color };
            changeColourOfLine(line, color);
        } else {
            line._perspectiveStyle = {
                color: line._computedStyle.color
            };
        }
    }

    this.getCurrentView = function() {
        return currentView;
    };

    this.getCurrentViewOrFilter = function() {
        if (currentFilter) {
            return currentFilter;
        } else {
            return currentView;
        }
    };

    this.isEditable = function() {
        return editable;
    };

    this.setEmbedded = function(bool) {
        embedded = bool;
    };

    this.isEmbedded = function() {
        return embedded;
    };

    this.setNavigationEnabled = function(bool) {
        navigationEnabled = bool;
    };

    this.isNavigationEnabled = function() {
        return navigationEnabled;
    };

    this.setDarkMode = function(bool) {
        darkMode = (bool === true);

        if (darkMode === true) {
            canvasColor = canvasColorDarkMode;
        } else {
            canvasColor = '#ffffff';
        }

        paper.drawBackground( { color: canvasColor });
        renderView();
    };

    this.isDarkMode = function() {
        return darkMode;
    };

    this.setPageSize = function(width, height) {
        width = Math.round(width);
        height = Math.round(height);

        diagramWidth = width;
        diagramHeight = height;
        currentView.dimensions = { width: width, height: height };
        paper.setDimensions(width, height);

        this.resize();

        repositionDiagramMetadata();

        this.zoomToWidthOrHeight();

        if (typeof postDiagramAspectRatioToParentWindow === 'function') {
            postDiagramAspectRatioToParentWindow(true);
        }
    };

    function setZoomAndMargins() {
        minZoomScale = scale;
        maxZoomScale = DEFAULT_MAX_ZOOM_SCALE;
        zoomDelta = (maxZoomScale - minZoomScale) / zoomSteps;

        var minimumWidth = minZoomScale * diagramWidth;
        var minimumHeight = minZoomScale * diagramHeight;

        var viewportWidth = viewport.innerWidth();
        var viewportHeight = viewport.innerHeight();

        var maxMarginLeftRight = Math.max(0, ((viewportWidth - minimumWidth)/2));
        canvas.css('margin-left', maxMarginLeftRight);
        canvas.css('margin-right', maxMarginLeftRight);

        var maxMarginTopBottom = Math.max(0, ((viewportHeight - minimumHeight)/2));
        canvas.css('margin-top', maxMarginTopBottom);
        canvas.css('margin-bottom', maxMarginTopBottom);
    }

    this.decreasePageSize = function(evt) {
        currentView.paperSize = undefined;
        this.setPageSize(Math.max(diagramWidth - pageSizeDelta, diagramMetadataWidth), diagramHeight - pageSizeDelta);

        if (evt.altKey === false) {
            centreDiagram();
        }
    };

    this.autoPageSize = function() {
        var margin = 400;
        var contentArea = findContentArea(false, 0);

        var contentWidth = contentArea.maxX - contentArea.minX + margin;
        var contentHeight = contentArea.maxY - contentArea.minY + margin;

        contentWidth = Math.max(contentWidth, diagramMetadataWidth);

        this.setPageSize(contentWidth, contentHeight);

        centreDiagram();
    };

    this.increasePageSize = function(evt) {
        currentView.paperSize = undefined;
        this.setPageSize(diagramWidth + pageSizeDelta, diagramHeight + pageSizeDelta);

        if (evt.altKey === false) {
            centreDiagram();
        }
    };

    this.getWidth = function() {
        return diagramWidth;
    };

    this.getHeight = function() {
        return diagramHeight;
    };

    this.getAspectRatio = function() {
        return diagramWidth / diagramHeight;
    }

    function renderElementInternals(element, cell, configuration, width, horizontalOffset, height, verticalOffset) {
        const defaultIconWidth = 60;
        const defaultIconHeight = 60;
        const horizontalIconPadding = 15;

        const horizontalPadding = 30;
        const fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        const color = structurizr.util.shadeColor(configuration.color, 100-configuration.opacity, darkMode);
        const stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);
        const navigationColor = color;

        var maxWidth;
        if (configuration.iconPosition === 'Left') {
            if (configuration.icon !== undefined) {
                maxWidth = width - (horizontalOffset + horizontalPadding + horizontalPadding + defaultIconWidth + horizontalIconPadding);
            } else {
                maxWidth = width - (horizontalOffset + horizontalPadding + horizontalPadding);
            }
        } else {
            maxWidth = width - (horizontalOffset + horizontalPadding + horizontalPadding);
        }

        var widthOfIcon = 0;
        var heightOfIcon = 0;
        if (configuration.icon !== undefined) {
            var iconRatio = getImageRatio(configuration.icon);

            if (configuration.iconPosition === 'Left') {
                widthOfIcon = defaultIconWidth;
                heightOfIcon = ((widthOfIcon) * iconRatio);
            } else {
                heightOfIcon = defaultIconHeight;
                widthOfIcon = ((heightOfIcon) * iconRatio);
            }
        }

        const name = formatName(element, configuration, maxWidth);
        const nameHeight = calculateHeight(name, configuration.fontSize * nameFontSizeDifferenceRatio, 0);
        const metadata = formatMetaData(element, configuration, maxWidth);
        const metadataHeight = calculateHeight(metadata, configuration.fontSize * metadataFontSizeDifferenceRatio, 0);
        const description = formatDescription(element, configuration, maxWidth);
        const descriptionHeight = calculateHeight(description, configuration.fontSize, 0);

        var y = 0;
        var totalY = 0;

        var iconY = 0;
        var nameY = 0;
        var metadataY = 0;
        var descriptionY = 0;

        if (configuration.icon !== undefined && configuration.iconPosition === 'Top') {
            const padding = 10;
            totalY += heightOfIcon;

            iconY = y;
            y += heightOfIcon;
            y += padding;
        }

        nameY = y;
        y += nameHeight;
        totalY += nameHeight;

        if (metadata.length > 0) {
            const padding = 8;
            y += padding;
            totalY += padding + metadataHeight;

            metadataY = y;
            y += metadataHeight;
        }

        if (description.length > 0) {
            const padding = 15;
            y += padding;
            totalY += padding + descriptionHeight;

            descriptionY = y;
            y += descriptionHeight;
        }

        if (configuration.icon !== undefined && configuration.iconPosition === 'Bottom') {
            const padding = 15;
            y += padding;
            totalY += padding + heightOfIcon;

            iconY = y;
        }

        const marginY = (height - totalY) / 2;

        if (totalY > height) {
            console.log('The height of the element named "' + element.name + '" is too small to fit the content (' + Math.ceil(totalY) + 'px)');
        }

        iconY += (verticalOffset + marginY);
        nameY += (verticalOffset + marginY);
        metadataY += (verticalOffset + marginY);
        descriptionY += (verticalOffset + marginY);

        var navigationRefY =    (height - 32) / height;

        if (configuration.icon && configuration.iconPosition !== 'Left') {
            const iconRefX = (((width - widthOfIcon) / 2) / width);

            cell.attributes.attrs['.structurizrIcon']['xlink:href'] = getImageMetadata(configuration.icon).dataURL;
            cell.attributes.attrs['.structurizrIcon']['width'] = widthOfIcon;
            cell.attributes.attrs['.structurizrIcon']['height'] = heightOfIcon;
            cell.attributes.attrs['.structurizrIcon']['ref-x'] = iconRefX;
            cell.attributes.attrs['.structurizrIcon']['ref-y'] = undefined;
            cell.attributes.attrs['.structurizrIcon']['y'] = iconY;
            cell.attributes.attrs['.structurizrIcon']['opacity'] = (configuration.opacity / 100);
        }

        cell.attributes.attrs['.structurizrName']['text'] = name;
        cell.attributes.attrs['.structurizrName']['font-family'] = font.name;
        cell.attributes.attrs['.structurizrName']['fill'] = color;
        cell.attributes.attrs['.structurizrName']['dominant-baseline'] = 'hanging';
        cell.attributes.attrs['.structurizrName']['font-size'] = (configuration.fontSize*nameFontSizeDifferenceRatio) + 'px';
        cell.attributes.attrs['.structurizrName']['ref-y'] = undefined;
        cell.attributes.attrs['.structurizrName']['y'] = nameY;
        cell.attributes.attrs['.structurizrName']['lineHeight'] = lineHeight;

        cell.attributes.attrs['.structurizrMetaData']['text'] = metadata;
        cell.attributes.attrs['.structurizrMetaData']['font-family'] = font.name;
        cell.attributes.attrs['.structurizrMetaData']['fill'] = color;
        cell.attributes.attrs['.structurizrMetaData']['dominant-baseline'] = 'hanging';
        cell.attributes.attrs['.structurizrMetaData']['font-size'] = (configuration.fontSize*metadataFontSizeDifferenceRatio) + 'px';
        cell.attributes.attrs['.structurizrMetaData']['ref-y'] = undefined;
        cell.attributes.attrs['.structurizrMetaData']['y'] = metadataY;
        cell.attributes.attrs['.structurizrMetaData']['lineHeight'] = lineHeight;

        cell.attributes.attrs['.structurizrDescription']['text'] = description;
        cell.attributes.attrs['.structurizrDescription']['font-family'] = font.name;
        cell.attributes.attrs['.structurizrDescription']['fill'] = color;
        cell.attributes.attrs['.structurizrDescription']['dominant-baseline'] = 'hanging';
        cell.attributes.attrs['.structurizrDescription']['font-size'] = (configuration.fontSize) + 'px';
        cell.attributes.attrs['.structurizrDescription']['ref-y'] = undefined;
        cell.attributes.attrs['.structurizrDescription']['y'] = descriptionY;
        cell.attributes.attrs['.structurizrDescription']['lineHeight'] = lineHeight;

        if (configuration.iconPosition === 'Left') {
            if (configuration.icon) {
                if (heightOfIcon > totalY) {
                    iconY = verticalOffset + ((height - heightOfIcon) / 2);
                } else {
                    iconY = nameY + 3;
                }

                cell.attributes.attrs['.structurizrIcon']['xlink:href'] = getImageMetadata(configuration.icon).dataURL;
                cell.attributes.attrs['.structurizrIcon']['width'] = widthOfIcon;
                cell.attributes.attrs['.structurizrIcon']['height'] = heightOfIcon;
                cell.attributes.attrs['.structurizrIcon']['x'] = horizontalOffset + horizontalPadding;
                cell.attributes.attrs['.structurizrIcon']['y'] = iconY;
                cell.attributes.attrs['.structurizrIcon']['opacity'] = (configuration.opacity / 100);
            }

            cell.attributes.attrs['.structurizrName']['text-anchor'] = 'start';
            cell.attributes.attrs['.structurizrName']['ref-x'] = undefined;
            cell.attributes.attrs['.structurizrName']['x'] = horizontalOffset + horizontalPadding + (widthOfIcon > 0 ? widthOfIcon + horizontalIconPadding : 0);

            cell.attributes.attrs['.structurizrMetaData']['text-anchor'] = 'start';
            cell.attributes.attrs['.structurizrMetaData']['ref-x'] = undefined;
            cell.attributes.attrs['.structurizrMetaData']['x'] = horizontalOffset + horizontalPadding + (widthOfIcon > 0 ? widthOfIcon + horizontalIconPadding : 0);

            cell.attributes.attrs['.structurizrDescription']['text-anchor'] = 'start';
            cell.attributes.attrs['.structurizrDescription']['ref-x'] = undefined;
            cell.attributes.attrs['.structurizrDescription']['x'] = horizontalOffset + horizontalPadding + (widthOfIcon > 0 ? widthOfIcon + horizontalIconPadding : 0);
        }

        cell.attributes.attrs['.structurizrNavigation']['color'] = navigationColor;
        cell.attributes.attrs['.structurizrNavigation']['ref-y'] = navigationRefY;

        cell._computedStyle = {};
        cell._computedStyle.width = width;
        cell._computedStyle.height = height;
        cell._computedStyle.background = fill;
        cell._computedStyle.color = color;
        cell._computedStyle.borderStyle = configuration.border;
        cell._computedStyle.stroke = stroke;
        cell._computedStyle.opacity = configuration.opacity;
    }

    function createBox(view, element, configuration, x, y, cornerRadius) {
        var width = configuration.width;
        var height = configuration.height;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Box({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrBox': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: cornerRadius,
                    ry: cornerRadius
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createEllipse(view, element, configuration, x, y, circle) {
        if (circle) {
            configuration.height = configuration.width;
        }

        var width = configuration.width;
        var height = configuration.height;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Ellipse({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrEllipse': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    cx: configuration.width/2,
                    cy: configuration.height/2,
                    rx: configuration.width/2,
                    ry: configuration.height/2
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, (width*0.9), 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createHexagon(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = Math.floor((configuration.width/2) * Math.sqrt(3));

        var points =    (configuration.width/4) + ",0 " +
                        (3*(configuration.width/4)) + ",0 " +
                        configuration.width + "," + (height/2) + " " +
                        (3*(configuration.width/4)) + "," + height + " " +
                        (configuration.width/4) + "," + height + " " +
                        "0," + (height/2);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Hexagon({
            position: {
                x: x,
                y: y
            },
            size: {
                width: width,
                height: height
            },
            attrs: {
                '.structurizrHexagon': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    points: points
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createDiamond(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.width;

        var points =
            (width/2) + ",0 " +
            width + "," + (height/2) + " " +
            (width/2) + "," + height + " " +
            "0," + (height/2);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Diamond({
            position: {
                x: x,
                y: y
            },
            size: {
                width: width,
                height: height
            },
            attrs: {
                '.structurizrDiamond': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    points: points
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createPerson(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.width - (configuration.width/2.5);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Person({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.width
            },
            attrs: {
                '.structurizrPersonHead': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    cx: configuration.width/2,
                    cy: configuration.width/4.5,
                    r: configuration.width/4.5
                },
                '.structurizrPersonBody': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 0,
                    y: configuration.width/2.5,
                    width: configuration.width,
                    height: height
                },
                '.structurizrPersonRightArm': {
                    stroke: stroke,
                    'stroke-width': 1,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x1: configuration.width/5,
                    y1: configuration.width/1.5,
                    x2: configuration.width/5,
                    y2: configuration.width
                },
                '.structurizrPersonLeftArm': {
                    stroke: stroke,
                    'stroke-width': 1,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x1: configuration.width-(configuration.width/5),
                    y1: configuration.width/1.5,
                    x2: configuration.width-(configuration.width/5),
                    y2: configuration.width
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, configuration.width/2.5);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createRobot(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.width - (configuration.width/2.5);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Robot({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.width
            },
            attrs: {
                '.structurizrRobotHead': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: (configuration.width - configuration.width/2.25)/2,
                    y: 0,
                    width: configuration.width/2.25,
                    height: configuration.width/2.25
                },
                '.structurizrRobotEars': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: (configuration.width - configuration.width/1.8)/2,
                    y: (configuration.width/2.25 - configuration.width/10)/2,
                    width: configuration.width/1.8,
                    height: configuration.width/10
                },
                '.structurizrRobotBody': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 0,
                    y: configuration.width/2.5,
                    width: configuration.width,
                    height: height
                },
                '.structurizrRobotRightArm': {
                    stroke: stroke,
                    'stroke-width': 1,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x1: configuration.width/5,
                    y1: configuration.width/1.5,
                    x2: configuration.width/5,
                    y2: configuration.width
                },
                '.structurizrRobotLeftArm': {
                    stroke: stroke,
                    'stroke-width': 1,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x1: configuration.width-(configuration.width/5),
                    y1: configuration.width/1.5,
                    x2: configuration.width-(configuration.width/5),
                    y2: configuration.width
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, configuration.width/2.5);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createCylinder(view, element, configuration, x, y) {
        var ry = 60;
        var width = configuration.width;
        var height = configuration.height - (ry/2);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        // M 0,10
        // a 50,10 0,0,0 100 0
        // a 50,10 0,0,0 -100 0
        // l 0,60
        // a 50,10 0,0,0 100 0
        // l 0,-60
        var path = 'M 0,' + (ry/2);
        path += ' a ' + (width / 2) + ',' + (ry/2) + ' 0,0,0 ' + width + ' ' + 0;
        path += ' a ' + (width / 2) + ',' + (ry/2) + ' 0,0,0 -' + width + ' ' + 0;
        path += ' l 0,' + (configuration.height - ry);
        path += ' a ' + (width / 2) + ',' + (ry/2) + ' 0,0,0 ' + width + ' ' + 0;
        path += ' l 0,-' + (configuration.height - ry);

        var cell = new structurizr.shapes.Cylinder({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrCylinderPath': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    d: path
                },
                '.structurizrCylinderFace': {
                    fill: 'none',
                    stroke: 'none',
                    width: configuration.width,
                    height: (configuration.height - (ry/2)),
                    x: 0,
                    y: (ry/2)
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 30);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createBucket(view, element, configuration, x, y) {
        var ry = 60;
        var width = configuration.width;
        var height = configuration.height - (ry/2);

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var path = 'M 0,' + (ry/2);
        path += ' a ' + (width / 2) + ',' + (ry/2) + ' 0,0,0 ' + width + ' ' + 0;
        path += ' a ' + (width / 2) + ',' + (ry/2) + ' 0,0,0 -' + width + ' ' + 0;
        path += ' l ' + (configuration.width/10) + ',' + (configuration.height - ry);
        path += ' a ' + (width / 2) + ',' + (ry) + ' 0,0,0 ' + (width - (width*0.2)) + ' ' + 0;
        path += ' l ' + (configuration.width/10) + ',-' + (configuration.height - ry);

        var cell = new structurizr.shapes.Cylinder({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrCylinderPath': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    d: path
                },
                '.structurizrCylinderFace': {
                    fill: 'none',
                    stroke: 'none',
                    width: configuration.width,
                    height: (configuration.height - (ry/2)),
                    x: 0,
                    y: (ry/2)
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, (width*0.9), 0, height, 30);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createPipe(view, element, configuration, x, y) {
        var rx = 60;
        var width = configuration.width - (rx/2);
        var height = configuration.height;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        // M 10,0
        // a 10,50 0,0,1 0 100
        // a 10,50 0,0,1 0 -100
        // l 60,0
        // a 10,50 0,0,1 0 100
        // l -60,0
        var path = 'M ' + (rx/2) + ',0';
        path += ' a ' + (rx/2) + ',' + (height / 2) + ' 0,0,1 0 ' + height;
        path += ' a ' + (rx/2) + ',' + (height / 2) + ' 0,0,1 0 -' + height;
        path += ' l ' + (configuration.width - rx) + ',0';
        path += ' a ' + (rx/2) + ',' + (height / 2) + ' 0,0,1 0 ' + height;
        path += ' l -' + (configuration.width - rx) + ',0';

        var cell = new structurizr.shapes.Pipe({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrPipePath': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    d: path
                },
                '.structurizrPipeFace': {
                    fill: 'none',
                    stroke: 'none',
                    width: configuration.width-(rx/2),
                    height: configuration.height,
                    x: (rx/2),
                    y: 0
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, rx, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createFolder(view, element, configuration, x, y) {
        var tabHeight = configuration.height/8;
        var tabWidth = configuration.width/3;
        var width = configuration.width;
        var height = configuration.height - tabHeight;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Folder({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrFolderTab': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 10,
                    y: 0,
                    width: tabWidth,
                    height: tabHeight*2,
                    rx: 10,
                    ry: 10,
                },
                '.structurizrFolderBody': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 0,
                    y: tabHeight,
                    width: configuration.width,
                    height: height,
                    rx: 5,
                    ry: 5
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, tabHeight);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createComponent(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.height;
        var blockWidth = width / 6;
        var blockHeight = height / 8;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Component({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrComponent': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: (blockWidth / 2),
                    width: configuration.width - (blockWidth / 2),
                    height: height,
                    rx: 10,
                    ry: 10
                },
                '.structurizrComponentBlockTop': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 0,
                    y: blockHeight * 0.6,
                    width: blockWidth,
                    height: blockHeight,
                    rx: 5,
                    ry: 5
                },
                '.structurizrComponentBlockBottom': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    x: 0,
                    y: blockHeight * 2,
                    width: blockWidth,
                    height: blockHeight,
                    rx: 5,
                    ry: 5
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width-20, blockWidth, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createWebBrowser(view, element, configuration, x, y) {
        const heightOfWindowControls = 40;
        var width = configuration.width;
        var height = configuration.height + configuration.strokeWidth;
        var webBrowserPanelWidth = configuration.width - (configuration.strokeWidth * 2);
        var webBrowserPanelHeight = height - heightOfWindowControls - configuration.strokeWidth;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.WebBrowser({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrWebBrowser': {
                    fill: stroke,
                    stroke: stroke,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWebBrowserPanel': {
                    fill: fill,
                    stroke: stroke,
                    width: webBrowserPanelWidth,
                    height: webBrowserPanelHeight,
                    x: configuration.strokeWidth,
                    y: heightOfWindowControls,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWebBrowserUrlBar': {
                    fill: fill,
                    width: configuration.width - 110,
                    height: 20,
                    x: 100,
                    y: 10,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWebBrowserButton1': {
                    fill: fill,
                    cx: 20,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWebBrowserButton2': {
                    fill: fill,
                    cx: 50,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWebBrowserButton3': {
                    fill: fill,
                    cx: 80,
                    cy: 20,
                    rx: 10,
                    ry: 10
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, webBrowserPanelWidth, 0, webBrowserPanelHeight, 40);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createWindow(view, element, configuration, x, y) {
        const heightOfWindowControls = 40;
        var width = configuration.width;
        var height = configuration.height + configuration.strokeWidth;
        var windowPanelWidth = configuration.width - (configuration.strokeWidth * 2);
        var windowPanelHeight = height - heightOfWindowControls - configuration.strokeWidth;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Window({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrWindow': {
                    fill: stroke,
                    stroke: stroke,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWindowPanel': {
                    fill: fill,
                    stroke: stroke,
                    width: windowPanelWidth,
                    height: windowPanelHeight,
                    x: configuration.strokeWidth,
                    y: heightOfWindowControls,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWindowButton1': {
                    fill: fill,
                    cx: 20,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWindowButton2': {
                    fill: fill,
                    cx: 50,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrWindowButton3': {
                    fill: fill,
                    cx: 80,
                    cy: 20,
                    rx: 10,
                    ry: 10
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, windowPanelWidth, 0, windowPanelHeight, 40);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createTerminal(view, element, configuration, x, y) {
        const heightOfWindowControls = 40;
        var width = configuration.width;
        var height = configuration.height + configuration.strokeWidth;
        var windowPanelWidth = configuration.width - (configuration.strokeWidth * 2);
        var windowPanelHeight = height - heightOfWindowControls - configuration.strokeWidth;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Terminal({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrTerminal': {
                    fill: stroke,
                    stroke: stroke,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 10,
                    ry: 10
                },
                '.structurizrTerminalPanel': {
                    fill: fill,
                    stroke: stroke,
                    width: windowPanelWidth,
                    height: windowPanelHeight,
                    x: configuration.strokeWidth,
                    y: heightOfWindowControls,
                    rx: 10,
                    ry: 10
                },
                '.structurizrTerminalButton1': {
                    fill: fill,
                    cx: 20,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrTerminalButton2': {
                    fill: fill,
                    cx: 50,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrTerminalButton3': {
                    fill: fill,
                    cx: 80,
                    cy: 20,
                    rx: 10,
                    ry: 10
                },
                '.structurizrTerminalPrompt': {
                    fill: stroke,
                    rx: 20,
                    ry: 20
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, windowPanelWidth, 0, windowPanelHeight, 40);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createShell(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.height;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var cell = new structurizr.shapes.Shell({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrShell': {
                    fill: fill,
                    stroke: stroke,
                    'stroke-width': configuration.strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 10,
                    ry: 10
                },
                '.structurizrShellPrompt': {
                    fill: stroke,
                    rx: 20,
                    ry: 20
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createMobileDevicePortrait(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.height;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var speakerLength = 50;

        var cell = new structurizr.shapes.MobileDevice({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrMobileDevice': {
                    fill: stroke,
                    stroke: stroke,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 20,
                    ry: 20
                },
                '.structurizrMobileDeviceDisplay': {
                    fill: fill,
                    stroke: stroke,
                    width: configuration.width - 20,
                    height: configuration.height - 80,
                    x: 10,
                    y: 40,
                    rx: 5,
                    ry: 5
                },
                '.structurizrMobileDeviceButton': {
                    fill: fill,
                    cx: (configuration.width/2),
                    cy: (height-20),
                    rx: 10,
                    ry: 10
                },
                '.structurizrMobileDeviceSpeaker': {
                    stroke: fill,
                    x1: (configuration.width - speakerLength) / 2,
                    y1: 20,
                    x2: configuration.width - ((configuration.width - speakerLength) / 2),
                    y2: 20
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createMobileDeviceLandscape(view, element, configuration, x, y) {
        var width = configuration.width;
        var height = configuration.height + configuration.strokeWidth;

        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);

        var speakerLength = 50;

        var cell = new structurizr.shapes.MobileDevice({
            position: {
                x: x,
                y: y
            },
            size: {
                width: configuration.width,
                height: configuration.height
            },
            attrs: {
                '.structurizrMobileDevice': {
                    fill: stroke,
                    stroke: stroke,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    width: configuration.width,
                    height: height,
                    rx: 20,
                    ry: 20
                },
                '.structurizrMobileDeviceDisplay': {
                    fill: fill,
                    stroke: stroke,
                    width: configuration.width - 80,
                    height: height - 20,
                    x: 40,
                    y: 10,
                    rx: 5,
                    ry: 5
                },
                '.structurizrMobileDeviceButton': {
                    fill: fill,
                    cx: 20,
                    cy: (height/2),
                    rx: 10,
                    ry: 10
                },
                '.structurizrMobileDeviceSpeaker': {
                    stroke: fill,
                    x1: configuration.width - 20,
                    y1: (configuration.height - speakerLength) / 2,
                    x2: configuration.width - 20,
                    y2: configuration.height - ((configuration.height - speakerLength) / 2)
                }
            },
            element: element
        });

        renderElementInternals(element, cell, configuration, width, 0, height, 0);

        graph.addCell(cell);
        mapOfIdToBox[element.id] = cell;

        return cell;
    }

    function createDiagramMetadata() {
        const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
        const showTitle = getViewOrViewSetProperty(viewOrFilter, 'structurizr.title', 'true') === 'true';
        const showDescription = getViewOrViewSetProperty(viewOrFilter, 'structurizr.description', 'true') === 'true';
        const showMetadata = getViewOrViewSetProperty(viewOrFilter, 'structurizr.metadata', 'true') === 'true';

        diagramTitle = '';
        diagramDescription = '';
        diagramMetadata = '';
        diagramMetadataWidth = 0;

        if (showTitle) {
            diagramTitle = structurizr.util.removeNewlineCharacters(structurizr.ui.getTitleForView(viewOrFilter));
        }

        diagramTitleElement = new structurizr.shapes.DiagramTitle({
            attrs: {
                '.structurizrDiagramTitle': {
                    text: diagramTitle,
                    'font-size': elementStyleForDiagramTitle.fontSize + 'px',
                    'font-family': font.name,
                    fill: elementStyleForDiagramTitle.color,
                    'lineHeight': lineHeight
                }
            }});
        graph.addCell(diagramTitleElement);
        diagramTitleElement.toBack();
        diagramMetadataWidth = Math.max(diagramMetadataWidth, calculateWidth(diagramTitle, elementStyleForDiagramTitle.fontSize));

        if (showDescription) {
            if (currentFilter && currentFilter.description) {
                diagramDescription = currentFilter.description;
            } else if (currentView.description) {
                diagramDescription = currentView.description;
            }
        }

        if (diagramDescription !== undefined && diagramDescription.length > 0) {
            diagramDescriptionElement = new structurizr.shapes.DiagramDescription({
                attrs: {
                    '.structurizrDiagramDescription': {
                        text: diagramDescription,
                        'font-size': elementStyleForDiagramDescription.fontSize + 'px',
                        'font-family': font.name,
                        fill: elementStyleForDiagramDescription.color,
                        'lineHeight': lineHeight
                    }
                }
            });

            graph.addCell(diagramDescriptionElement);
            diagramDescriptionElement.toBack();
            diagramMetadataWidth = Math.max(diagramMetadataWidth, calculateWidth(diagramDescription, elementStyleForDiagramDescription.fontSize));
        } else {
            diagramDescriptionElement = undefined;
        }

        const timezone = structurizr.workspace.views.configuration.properties['structurizr.timezone'];
        const locale = structurizr.workspace.views.configuration.properties['structurizr.locale'];
        const options = {
            weekday: 'long',
            year:'numeric',
            month: 'long',
            day: 'numeric',
            hour: 'numeric',
            minute: 'numeric',
            timeZone: timezone,
            timeZoneName : 'long'
        };

        if (showMetadata) {
            if (structurizr.workspace.id === 0) {
                // playground
                diagramMetadata = new Date().toLocaleString(locale, options);
            } else {
                const lastModified = structurizr.workspace.lastModifiedDate;
                if (lastModified) {
                    diagramMetadata = new Date(lastModified).toLocaleString(locale, options);
                }

                const version = structurizr.workspace.version;
                if (version) {
                    if (diagramMetadata) {
                        diagramMetadata += ' | ';
                    } else {
                        diagramMetadata = '';
                    }
                    diagramMetadata += 'Version: ' + version;
                }
            }
        }

        diagramMetadataElement = new structurizr.shapes.DiagramMetadata({
            attrs: {
                '.structurizrDiagramMetadata': {
                    text: diagramMetadata,
                    'font-size': elementStyleForDiagramMetadata.fontSize + 'px',
                    'font-family': font.name,
                    fill: elementStyleForDiagramMetadata.color,
                    'lineHeight': lineHeight
                }
            }});
        graph.addCell(diagramMetadataElement);
        diagramMetadataElement.toBack();
        diagramMetadataWidth = Math.max(diagramMetadataWidth, calculateWidth(diagramMetadata, elementStyleForDiagramMetadata.fontSize));

        const padding = 10;
        const titleHeight = calculateHeight(diagramTitle, elementStyleForDiagramTitle.fontSize, 0) + padding;
        const descriptionHeight = calculateHeight(diagramDescription, elementStyleForDiagramDescription.fontSize, 0) + padding;
        const metadataHeight = calculateHeight(diagramMetadata, elementStyleForDiagramMetadata.fontSize, 0) + padding;

        if (showMetadata === true) {
            diagramMetadataHeight += metadataHeight;
        }

        if (diagramDescriptionElement !== undefined && showDescription === true) {
            diagramMetadataHeight += descriptionHeight;
        }

        if (showTitle === true) {
            diagramMetadataHeight += titleHeight;
        }

        var icon = structurizr.ui.findElementStyle( {
            type: undefined,
            tags: 'Workspace:Icon'
        }, darkMode).icon;

        if (icon) {
            brandingLogo = new structurizr.shapes.BrandingImage({
                size: { width: getImageRatio(icon) * diagramMetadataHeight, height: diagramMetadataHeight },
                attrs: {
                    image: {
                        'xlink:href': getImageMetadata(icon).dataURL,
                        width: getImageRatio(icon) * diagramMetadataHeight,
                        height: diagramMetadataHeight
                    }
                }
            });
            graph.addCell(brandingLogo);
            brandingLogo.toBack();
        }
    }

    function getViewOrViewSetProperty(view, name, defaultValue) {
        var value = defaultValue;

        if (structurizr.workspace.views.configuration.properties && structurizr.workspace.views.configuration.properties[name]) {
            value = structurizr.workspace.views.configuration.properties[name];
        }

        if (view.properties && view.properties[name]) {
            value = view.properties[name];
        }

        return value;
    }

    function repositionDiagramMetadata() {
        if (elementStyleForDiagramTitle === undefined) {
            return;
        }

        const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
        const showTitle = getViewOrViewSetProperty(viewOrFilter, 'structurizr.title', 'true') === 'true';
        const showDescription = getViewOrViewSetProperty(viewOrFilter, 'structurizr.description', 'true') === 'true';
        const showMetadata = getViewOrViewSetProperty(viewOrFilter, 'structurizr.metadata', 'true') === 'true';

        const paddingLeft = 20;
        const paddingBottom = 10;
        const paddingLogoRight = 40;

        const padding = 10;
        const titleHeight = calculateHeight(diagramTitle, elementStyleForDiagramTitle.fontSize, 0) + padding;
        const descriptionHeight = calculateHeight(diagramDescription, elementStyleForDiagramDescription.fontSize, 0) + padding;
        const metadataHeight = calculateHeight(diagramMetadata, elementStyleForDiagramMetadata.fontSize, 0) + padding;

        var x = paddingLeft;
        var y = diagramHeight - paddingBottom;

        if (brandingLogo !== undefined) {
            brandingLogo.set(
                {
                    position: { x: x, y: y - diagramMetadataHeight }
                }
            );
            x = brandingLogo.get('size').width + paddingLogoRight;
        }

        if (diagramMetadataElement !== undefined && showMetadata === true) {
            y = y - metadataHeight;
            diagramMetadataElement.set({position: {x: x, y: y}});
            $(".structurizrDiagramMetadata").attr('display', 'block');
        }

        if (diagramDescriptionElement !== undefined && showDescription === true) {
            y = y - descriptionHeight;
            diagramDescriptionElement.set({position: {x: x, y: y}});
            $(".structurizrDiagramDescription").attr('display', 'block');
        }

        if (diagramTitleElement !== undefined && showTitle === true) {
            y = y - titleHeight;
            diagramTitleElement.set({ position: { x: x, y: y }});
            $(".structurizrDiagramTitle").attr('display', 'block');
        }
    }

    function calculateHeight(text, fontSize, fontSizeDelta) {
        var lineSpacing = 1.20;
        if (text) {
            text = text.trim();

            if (text.length === 0) {
                return 0;
            } else {
                var numberOfLines = text.split("\n").length;
                return (fontSize + fontSizeDelta) + ((numberOfLines-1) * ((fontSize + fontSizeDelta) * lineSpacing));
            }
        } else {
            return 0;
        }
    }

    function calculateWidth(text, fontSize) {
        if (text) {
            text = text.trim();

            if (text.length === 0) {
                return 0;
            } else {
                var length = 0;
                text.split('\n').forEach(function(line) {
                    length = Math.max(length, line.length * (0.6 * fontSize));
                });

                return length;
            }
        } else {
            return 0;
        }
    }

    function formatName(element, configuration, width) {
        return breakText(element.name ? structurizr.util.removeNewlineCharacters(element.name) : "", Math.max(0, width), font.name, (configuration.fontSize * nameFontSizeDifferenceRatio));
    }

    function formatDescription(element, configuration, width) {
        if (configuration.description !== undefined && configuration.description === false) {
            return '';
        } else {
            return breakText(element.description ? element.description : "", Math.max(0, width), font.name, configuration.fontSize);
        }
    }

    function formatMetaData(element, configuration, width) {
        if (element.type === 'Custom' && (element.metadata === undefined || element.metadata === '')) {
            return '';
        }

        if (configuration.metadata !== undefined && configuration.metadata === false) {
            return '';
        } else {
            var metadata = breakText(structurizr.ui.getMetadataForElement(element, true), width, font.name, (configuration.fontSize * metadataFontSizeDifferenceRatio));

            if (currentView.type === 'Deployment') {
                if (element.type === 'ContainerInstance') {
                    var container = structurizr.workspace.findElementById(element.containerId);
                    if (container) {
                        if (currentView.softwareSystemId === undefined || (container.parentId !== currentView.softwareSystemId)) {
                            // this is a container from a "foreign" software system
                            var softwareSystem = structurizr.workspace.findElementById(container.parentId);
                            metadata = "from " + softwareSystem.name + "\n" + metadata;
                        }
                    }
                }
            }

            return metadata;
        }
    }

    function formatTechnologyForRelationship(relationship, configuration) {
        if (configuration.metadata !== undefined && configuration.metadata === false) {
            return '';
        } else {
            return structurizr.ui.getMetadataForRelationship(relationship);
        }
    }

    function breakText(text, width, font, fontSize) {
        var characterWidth = fontSize * 0.75;
        if (text && (text.length * characterWidth > width)) {
            return joint.util.breakText(text,
                {
                    width: width
                },
                {
                    'font': font,
                    'font-size': fontSize + 'px'
                }
            );
        } else {
            return text;
        }
    }

    function dashArrayForElement(elementStyle) {
        var dasharray;

        switch(elementStyle.border) {
            case 'Dashed':
                dasharray = (elementStyle.strokeWidth * 4) + ',' + (elementStyle.strokeWidth * 4);

                break;
            case 'Dotted':
                dasharray = (elementStyle.strokeWidth) + ',' + (elementStyle.strokeWidth * 2);

                break;
            default:
                dasharray = '';
        }

        return dasharray;
    }

    function dashArrayForRelationship(relationshipStyle) {
        var dasharray;

        switch(relationshipStyle.style) {
            case 'Dashed':
                dasharray = (relationshipStyle.thickness * 4) + ' ' + (relationshipStyle.thickness * 4);

                break;
            case 'Dotted':
                dasharray = (relationshipStyle.thickness) + ' ' + (relationshipStyle.thickness * 2);

                break;
            default:
                dasharray = '';
        }

        return dasharray;
    }

    function calculateArrowHead(thickness) {
        var size = (thickness * 10);
        if (size > 50) {
            size = 50;
        }

        // e.g. M 20 -10 0 0 20 10 Z
        return 'M ' + size + ' -' + (size/2) + ' 0 0 ' + size + ' ' + (size/2) + ' Z';
    }

    function removeIllegalElements() {
        try {
            if (currentView.type === 'Container') {
                // since a container diagram can show "foreign" containers, we need to make sure that the parent software system isn't also included on the diagram
                const softwareSystemIdsToRemove = [];
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element.type === 'Container' && element.parentId !== currentView.softwareSystemId) {
                        if (softwareSystemIdsToRemove.indexOf(element.parentId) === -1) {
                            softwareSystemIdsToRemove.push(element.parentId);
                        }
                    }
                });

                for (var i = 0; i < softwareSystemIdsToRemove.length; i++) {
                    for (var j = 0; j < currentView.elements.length; j++) {
                        if (currentView.elements[j].id === softwareSystemIdsToRemove[i]) {
                            console.log('Removing software system with ID ' + softwareSystemIdsToRemove[i] + ' because one of it\'s child containers is included on the diagram');
                            currentView.elements.splice(j, 1);
                        }
                    }
                }
            }

            if (currentView.type === 'Component') {
                // since a container diagram can show "foreign" containers and components, we need to make sure that the parent software system and/or container isn't also included on the diagram
                const softwareSystemIdsToRemove = [];
                const containerIdsToRemove = [];
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element.type === 'Component' && element.parentId !== currentView.containerId) {
                        if (containerIdsToRemove.indexOf(element.parentId) === -1) {
                            containerIdsToRemove.push(element.parentId);
                        }

                        var foreignContainer = structurizr.workspace.findElementById(element.parentId);
                        if (foreignContainer && softwareSystemIdsToRemove.indexOf(foreignContainer.parentId) === -1) {
                            softwareSystemIdsToRemove.push(foreignContainer.parentId);
                        }
                    } else if (element.type === 'Container' && element.id !== currentView.containerId) {
                        if (softwareSystemIdsToRemove.indexOf(element.parentId) === -1) {
                            softwareSystemIdsToRemove.push(element.parentId);
                        }
                    }
                });


                for (var i = 0; i < softwareSystemIdsToRemove.length; i++) {
                    for (var j = 0; j < currentView.elements.length; j++) {
                        if (currentView.elements[j].id === softwareSystemIdsToRemove[i]) {
                            console.log('Removing software system with ID ' + softwareSystemIdsToRemove[i] + ' because one of it\'s child containers is included on the diagram');
                            currentView.elements.splice(j, 1);
                        }
                    }
                }

                for (var i = 0; i < containerIdsToRemove.length; i++) {
                    for (var j = 0; j < currentView.elements.length; j++) {
                        if (currentView.elements[j].id === containerIdsToRemove[i]) {
                            console.log('Removing container with ID ' + containerIdsToRemove[i] + ' because one of it\'s child components is included on the diagram');
                            currentView.elements.splice(j, 1);
                        }
                    }
                }
            }
        } catch (err) {
            console.log(err);
        }
    }

    function includeElementOnDiagram(element) {
        return includeOnDiagram(structurizr.workspace.getAllTagsForElement(element));
    }

    function includeRelationshipOnDiagram(relationship) {
        return includeOnDiagram(structurizr.workspace.getAllTagsForRelationship(relationship));
    }

    function includeOnDiagram(tags) {
        if (currentFilter) {
            if (currentFilter.mode === "Include") {
                var include = false;
                if (currentFilter.tags) {
                    currentFilter.tags.forEach(function (tag) {
                        tag = tag.trim();
                        include = include || tags.indexOf(tag) > -1;
                    });
                }

                return include;
            } else {
                var exclude = false;
                if (currentFilter.tags) {
                    currentFilter.tags.forEach(function (tag) {
                        tag = tag.trim();
                        exclude = exclude || tags.indexOf(tag) > -1;
                    });
                }

                return !exclude;
            }
        } else {
            return true;
        }
    }

    function createArrow(relationshipInView) {
        var internalPadding = 10;
        var relationship = structurizr.workspace.findRelationshipById(relationshipInView.id);

        if (!includeRelationshipOnDiagram(relationship)) {
            return;
        }

        if (mapOfIdToBox[relationship.sourceId] && mapOfIdToBox[relationship.destinationId]) {
            var configuration;

            if (currentView.type === "Dynamic" && relationshipInView.response !== undefined && relationshipInView.response === true) {
                var originalTags = relationship.tags;
                relationship.tags = relationship.tags + ",Relationship/Response";
                configuration = structurizr.ui.findRelationshipStyle(relationship, darkMode);
                relationship.tags = originalTags;
            } else {
                configuration = structurizr.ui.findRelationshipStyle(relationship, darkMode);
            }

            registerRelationshipStyle(configuration);

            var triangle = calculateArrowHead(configuration.thickness);

            var description = "";
            if (currentView.type === structurizr.constants.DYNAMIC_VIEW_TYPE) {
                if (relationshipInView.description) {
                    description = relationshipInView.description;
                } else if (relationship.description) {
                    description = relationship.description;
                }

                if (relationshipInView.order) {
                    description = relationshipInView.order + ": " + description;
                }
            } else {
                if (relationship.description) {
                    description = relationship.description;
                }
            }

            if (configuration.description !== undefined && configuration.description === false) {
                description = '';
            } else {
                description = breakText(description, configuration.width, font.name, configuration.fontSize);
            }
            const heightOfDescription = calculateHeight(description, configuration.fontSize, 0);

            var technology = formatTechnologyForRelationship(relationship, configuration);
            technology = breakText(technology, configuration.width, font.name, configuration.fontSize * metadataFontSizeDifferenceRatio);
            const heightOfTechnology = calculateHeight(technology, configuration.fontSize * metadataFontSizeDifferenceRatio, 0);

            var totalHeight = heightOfDescription;
            if (heightOfTechnology > 0) {
                totalHeight += internalPadding;
                totalHeight += heightOfTechnology;
            }

            var fill = structurizr.util.shadeColor(configuration.color, 100 - configuration.opacity, darkMode);

            var routing = configuration.routing;
            if (relationshipInView.routing !== undefined) {
                routing = relationshipInView.routing;
            }

            var jump = configuration.jump;
            if (relationshipInView.jump !== undefined) {
                jump = relationshipInView.jump;
            }

            var position = configuration.position;
            if (relationshipInView.position !== undefined) {
                position = relationshipInView.position;
            }

            var sourceBox;
            var destinationBox;

            if (relationshipInView.response !== undefined && relationshipInView.response === true) {
                destinationBox = mapOfIdToBox[relationship.sourceId];
                sourceBox = mapOfIdToBox[relationship.destinationId];
            } else {
                sourceBox = mapOfIdToBox[relationship.sourceId];
                destinationBox = mapOfIdToBox[relationship.destinationId];
            }

            var labels = [];

            // description label
            labels.push({
                    position: {
                        distance: position / 100,
                        offset: { x: 0, y: (heightOfDescription / 2) - (totalHeight / 2) },
                        calculatedOffset: { x: 0, y: (heightOfDescription / 2) - (totalHeight / 2) }
                    },
                    attrs: {
                        rect: {
                            fill: canvasColor,
                            'pointer-events': 'none',
                            class: 'structurizrDescription'
                        },
                        text: {
                            text: description,
                            fill: fill,
                            'font-family': font.name,
                            'font-weight': 'normal',
                            'font-size': configuration.fontSize + 'px',
                            'pointer-events': 'none',
                            'lineHeight': lineHeight,
                            class: 'structurizrDescription'
                        }
                    }
                });

            // technology/metadata label
            if (technology && technology.trim().length > 0) {
                labels.push({
                    position: {
                        distance: position / 100,
                        offset: { x: 0, y: (totalHeight / 2) - (heightOfTechnology / 2) }
                    },
                    attrs: {
                        rect: {
                            'class': 'structurizrMetaData',
                            fill: canvasColor,
                            'pointer-events': 'none'
                        },
                        text: {
                            text: technology,
                            fill: fill,
                            'font-family': font.name,
                            'font-weight': 'normal',
                            'font-size': (configuration.fontSize * metadataFontSizeDifferenceRatio) + 'px',
                            'pointer-events': 'none',
                            'lineHeight': lineHeight,
                            class: 'structurizrMetaData'
                        }
                    }
                });
            }

            // navigation (e.g. URL indicator) label
            labels.push({
                position: {
                    distance: position / 100,
                    offset: { x: -10, y: totalHeight / 2}
                },
                attrs: {
                    rect: {
                        'class': 'structurizrNavigation'
                    }
                }
            });

            const link = new structurizr.shapes.Relationship({
                source: {
                    id: sourceBox.id
                },
                target: {
                    id: destinationBox.id
                },
                labels: labels
            });

            link.attr({
                line: {
                    stroke: fill,
                    'stroke-width': configuration.thickness,
                    'stroke-dasharray': dashArrayForRelationship(configuration),
                    'fill': 'none',
                    targetMarker: {
                        'type': 'path',
                        d: triangle
                    }
                }
            });

            setRouting(link, routing);
            setJump(link, jump, configuration.thickness);

            link.relationshipInView = relationshipInView;

            if (relationshipInView.vertices) {
                link.set('vertices', relationshipInView.vertices);
            }

            link.on('change:vertices', function (event) {
                var vertices = link.get('vertices');
                if (vertices) {
                    link.relationshipInView.vertices = vertices;
                    fireWorkspaceChangedEvent();
                }
            });

            graph.addCell(link);

            link._computedStyle = {};
            link._computedStyle.color = fill;
            link._computedStyle.lineStyle = configuration.style;

            if (relationshipInView.order) {
                mapOfIdToLine[relationshipInView.id + '/' + relationshipInView.order] = link;
            } else {
                mapOfIdToLine[relationshipInView.id] = link;
            }

            if (navigationEnabled) {
                addDoubleClickHandlerForRelationship(paper.findViewByModel(link), relationship);
            }

            if (editable) {
                const toolsView = new joint.dia.ToolsView({
                    tools: [
                        new joint.linkTools.Vertices({
                            vertexAdding: true,
                            vertexMoving: true,
                            vertexRemoving: true,
                            scale: 2
                        })
                        //new joint.linkTools.Segments()
                    ]
                });

                const linkView = link.findView(paper);
                linkView.addTools(toolsView);
            }

            link.set('labelSize', { width: configuration.width * 1.2, height: totalHeight });

            return link;
        } else {
            console.log("Not rendering relationship " + relationship.id + ' (' + structurizr.workspace.findElementById(relationship.sourceId).name + ' -> ' + structurizr.workspace.findElementById(relationship.destinationId).name + ') because the source and destination elements are not on the diagram.');
        }
    }

    function setRouting(link, routing) {
        if (routing === undefined || routing === 'Direct') {
            link.unset('router');
            link.connector('rounded');
        } else if (routing === 'Orthogonal') {
            link.set('router', { name: 'orthogonal' });
            link.connector('rounded');
        } else if (routing === 'Curved') {
            link.unset('router');
            link.connector('smooth');
        }
    }

    function setJump(link, jump, thickness) {
        if (jump !== undefined) {
            if (jump === true) {
                link.connector('jumpover', { size: 5 * thickness, radius: 5 * thickness } );
            } else {
                link.connector('rounded');
            }
        }
    }

    function adjustVertices(graph, link) {
        var srcId = link.get('source').id || link.previous('source').id;
        var trgId = link.get('target').id || link.previous('target').id;

        // if one of the ends is not a model, the link has no siblings
        if (!srcId || !trgId) return;

        var siblings = _.filter(graph.getLinks(), function(sibling) {

            var _srcId = sibling.get('source').id;
            var _trgId = sibling.get('target').id;

            return (_srcId === srcId && _trgId === trgId) || (_srcId === trgId && _trgId === srcId);
        });

        switch (siblings.length) {
            case 0:
                // the link was removed and had no siblings
                break;

            case 1:
                // there is only one link between the source and target - leave as-is
                break;

            default:
                var numberOfSiblingsWithoutVertices = 0;
                siblings.forEach(function(sibling) {
                   if (sibling.relationshipInView.vertices === undefined || sibling.relationshipInView.vertices.length === 0) {
                       numberOfSiblingsWithoutVertices++;
                   }
                });

                // if two more of the siblings don't have vertices, they're going to overlap
                if (numberOfSiblingsWithoutVertices >= 2) {
                    // There is more than one sibling, so we need to create vertices.
                    // First of all we'll find the middle point of the link.
                    var srcCenter = graph.getCell(srcId).getBBox().center();
                    var trgCenter = graph.getCell(trgId).getBBox().center();
                    var midPoint = joint.g.line(srcCenter, trgCenter).midpoint();

                    // Then find the angle it forms.
                    var theta = srcCenter.theta(trgCenter);

                    // This is the maximum distance between links
                    var gap = 150;

                    _.each(siblings, function (sibling, index) {

                        var offsetIndex = index;
                        if (siblings.length % 2 === 0) {
                            offsetIndex++;
                        }

                        // We want the offset values to be calculated as follows 0, 20, 20, 40, 40, 60, 60 ..
                        var offset = gap * Math.ceil(offsetIndex / 2);

                        // Now we need the vertices to be placed at points which are 'offset' pixels distant
                        // from the first link and forms a perpendicular angle to it. And as index goes up
                        // alternate left and right.
                        //
                        //  ^  odd indexes
                        //  |
                        //  |---->  index 0 line (straight line between a source center and a target center.
                        //  |
                        //  v  even indexes
                        var sign = index % 2 ? 1 : -1;
                        var angle = joint.g.toRad(theta + sign * 90);
                        // We found the vertex.
                        var vertex = joint.g.point.fromPolar(offset, angle, midPoint);
                        sibling.set('vertices', [{x: vertex.x, y: vertex.y}]);
                    });
                }
        }
    }

    function createBoundaryForGroup(name) {
        return createBoundary(name, undefined, 'Group', undefined);
    }

    function createBoundary(name, metadata, type, element) {
        var elementStyle;
        var textColor;
        var stroke;
        var fill;
        var icon;
        var strokeWidth = 2;
        var nameText = structurizr.util.removeNewlineCharacters(name);

        if (type === structurizr.constants.GROUP_ELEMENT_TYPE) {
            elementStyle = structurizr.ui.findElementStyle( {
                type: structurizr.constants.GROUP_ELEMENT_TYPE,
                tags: 'Group, Group:' + name
            }, darkMode);

            icon = elementStyle.icon;
            stroke = elementStyle.stroke;
            textColor = elementStyle.color;
            strokeWidth = elementStyle.strokeWidth;

            if (useNestedGroups()) {
                const separator = getGroupSeparator();
                nameText = name.substring(name.lastIndexOf(separator) + separator.length);
            }

            // and apply opacity
            textColor = structurizr.util.shadeColor(textColor, 100 - elementStyle.opacity, darkMode);
            stroke = structurizr.util.shadeColor(stroke, 100-elementStyle.opacity, darkMode);
            fill = structurizr.util.shadeColor(elementStyle.background, 100-elementStyle.opacity, darkMode);

            if (elementStyle.shape === 'RoundedBox') {
                // do nothing, this is permitted
            } else {
                // default to a regular box
                elementStyle.shape = 'Box';
            }
            registerElementStyle(elementStyle);
        } else if (type === 'Enterprise') { // for backwards compatibility with older workspaces
            elementStyle = structurizr.ui.findElementStyle( {
                type: structurizr.constants.GROUP_ELEMENT_TYPE,
                tags: 'Boundary, Boundary:Enterprise'
            }, darkMode);

            icon = elementStyle.icon;
            stroke = elementStyle.stroke;
            textColor = elementStyle.color;
            strokeWidth = elementStyle.strokeWidth;
            elementStyle.shape = 'Box';

            textColor = structurizr.util.shadeColor(textColor, 100 - elementStyle.opacity, darkMode);
            stroke = structurizr.util.shadeColor(stroke, 100 - elementStyle.opacity, darkMode);
            fill = structurizr.util.shadeColor(elementStyle.background, 100-elementStyle.opacity, darkMode);
        } else if (element !== undefined) {
            elementStyle = structurizr.ui.findElementStyle(element, darkMode);

            // use this as an override
            const elementStyleForBoundary = structurizr.ui.findElementStyle({
                type: structurizr.constants.BOUNDARY_ELEMENT_TYPE,
                tags: 'Boundary, Boundary:' + element.type
            }, darkMode);

            if (elementStyleForBoundary.icon !== undefined) {
                elementStyle.icon = elementStyleForBoundary.icon;
            }
            icon = elementStyle.icon;

            if (elementStyleForBoundary.background !== undefined) {
                elementStyle.background = elementStyleForBoundary.background;
            }
            fill = elementStyle.background;

            if (elementStyleForBoundary.stroke !== undefined) {
                elementStyle.stroke = elementStyleForBoundary.stroke;
            }
            stroke = elementStyle.stroke;

            if (elementStyleForBoundary.strokeWidth !== undefined) {
                elementStyle.strokeWidth = elementStyleForBoundary.strokeWidth;
            }
            strokeWidth = elementStyle.strokeWidth;

            if (elementStyleForBoundary.color !== undefined) {
                elementStyle.color = elementStyleForBoundary.color;
            } else {
                // check the default color isn't the same as the background
                if (elementStyle.color === elementStyle.background) {
                    // use the stroke instead
                    elementStyle.color = elementStyle.stroke;
                }
            }
            textColor = elementStyle.color;

            if (elementStyleForBoundary.shape !== undefined) {
                elementStyle.shape = elementStyleForBoundary.shape;
            }
            if (shapeHasRoundedCorners(elementStyle.shape)) {
                elementStyle.shape = 'RoundedBox';
            } else {
                // default to a regular box
                elementStyle.shape = 'Box';
            }

            if (elementStyleForBoundary.border !== undefined) {
                elementStyle.border = elementStyleForBoundary.border;
            }
            fill = elementStyle.background;

            if (elementStyleForBoundary.fontSize !== undefined) {
                elementStyle.fontSize = elementStyleForBoundary.fontSize;
            }

            elementStyle.tags = [ 'Boundary' ].concat(elementStyle.tags);
            registerElementStyle(elementStyle);

            textColor = structurizr.util.shadeColor(textColor, 100 - elementStyle.opacity, darkMode);
            stroke = structurizr.util.shadeColor(stroke, 100 - elementStyle.opacity, darkMode);
            fill = structurizr.util.shadeColor(fill, 100-elementStyle.opacity, darkMode);
        }

        const cornerRadius = (elementStyle.shape === 'Box' ? 0 : 20);

        var heightOfIcon = (elementStyle.fontSize * nameFontSizeDifferenceRatio);
        if (metadata !== undefined) {
            if (elementStyle !== undefined && elementStyle.metadata !== undefined && elementStyle.metadata === false) {
                metadata = '';
            } else {
                heightOfIcon = (heightOfIcon * 1.2) + (elementStyle.fontSize * metadataFontSizeDifferenceRatio);
            }
        } else {
            metadata = '';
        }

        const boundary = new structurizr.shapes.Boundary({
            size: { width: 400, height: 400 },
            attrs: {
                '.structurizrBoundary': {
                    stroke: stroke,
                    fill: fill,
                    opacity: editable ? 0.6 : 1.0,
                    'stroke-width': strokeWidth,
                    'stroke-dasharray': dashArrayForElement(elementStyle),
                    rx: cornerRadius,
                    ry: cornerRadius
                },
                '.structurizrName': {
                    text: nameText,
                    'font-family': font.name,
                    'font-weight': 'bold',
                    'font-size': (elementStyle.fontSize * nameFontSizeDifferenceRatio) + 'px',
                    fill: textColor
                },
                '.structurizrMetaData': {
                    text: metadata,
                    'font-family': font.name,
                    'font-size': (elementStyle.fontSize * metadataFontSizeDifferenceRatio) + 'px',
                    fill: textColor
                }
            }
        });

        graph.addCell(boundary);

        boundary.on('change:position', moveLinksBetweenElementsContainedWithin);

        boundary._computedStyle = {};
        boundary._computedStyle.background = fill;
        boundary._computedStyle.color = textColor;
        boundary._computedStyle.borderStyle = elementStyle.border;
        boundary._computedStyle.stroke = stroke;
        boundary._computedStyle.fontSize = elementStyle.fontSize;

        var cellView = paper.findViewByModel(boundary);
        const widthOfName = $('#' + cellView.id + ' .structurizrName')[0].getComputedTextLength();

        if (icon) {
            var iconRatio = getImageRatio(icon);
            var widthOfIcon = (heightOfIcon * iconRatio);

            boundary.attributes.attrs['.structurizrIcon']['xlink:href'] = getImageMetadata(icon).dataURL;
            boundary.attributes.attrs['.structurizrIcon']['width'] = widthOfIcon;
            boundary.attributes.attrs['.structurizrIcon']['height'] = heightOfIcon;
            boundary.attributes.attrs['.structurizrIcon']['opacity'] = (elementStyle.opacity/100);
            boundary._computedStyle.icon = icon;

            boundary._computedStyle.minimumWidth = widthOfIcon + 10 + widthOfName;
        } else {
            boundary._computedStyle.minimumWidth = widthOfName;
        }

        $('#' + cellView.id).attr('style', 'cursor: ' + (editable === true ? 'move' : 'default') + ' !important');

        return boundary;
    }

    function shapeHasRoundedCorners(shape) {
        return [
            'RoundedBox',
            'Folder',
            'WebBrowser',
            'Window',
            'Terminal',
            'Shell',
            'MobileDevicePortrait',
            'MobileDeviceLandscape',
            'Component'
        ].indexOf(shape) > -1;
    }

    function createDeploymentNode(element) {
        var configuration = structurizr.ui.findElementStyle(element, darkMode);
        configuration.shape = 'Box';
        registerElementStyle(configuration);

        var textColor = structurizr.util.shadeColor(configuration.color, 100-configuration.opacity, darkMode);
        var stroke = structurizr.util.shadeColor(configuration.stroke, 100-configuration.opacity, darkMode);
        var strokeWidth = configuration.strokeWidth;
        var fill = structurizr.util.shadeColor(configuration.background, 100-configuration.opacity, darkMode);

        var instanceCount = '';
        if (element.instances && element.instances !== '1') {
            instanceCount = 'x' + element.instances;
        }

        var metadata = '';
        var heightOfIcon = (configuration.fontSize * nameFontSizeDifferenceRatio);

        if (configuration.metadata !== undefined && configuration.metadata === false) {
            metadata = ''
        } else {
            metadata = structurizr.ui.getMetadataForElement(element, true);
            heightOfIcon = (heightOfIcon * 1.2) + (configuration.fontSize * metadataFontSizeDifferenceRatio);
        }

        var cell = new structurizr.shapes.DeploymentNode({
            attrs: {
                '.structurizrDeploymentNode': {
                    stroke: stroke,
                    'stroke-width': strokeWidth,
                    'stroke-dasharray': dashArrayForElement(configuration),
                    fill: fill,
                    opacity: editable ? 0.6 : 1.0
                },
                '.structurizrName': {
                    text: structurizr.util.removeNewlineCharacters(element.name),
                    'font-family': font.name,
                    'font-weight': 'bold',
                    'font-size': (configuration.fontSize * nameFontSizeDifferenceRatio) + 'px',
                    'fill': textColor
                },
                '.structurizrMetaData': {
                    text: metadata,
                    'font-family': font.name,
                    'font-size': (configuration.fontSize * metadataFontSizeDifferenceRatio) + 'px',
                    'fill': textColor
                },
                '.structurizrInstanceCount': {
                    text: instanceCount,
                    'font-family': font.name,
                    'fill': textColor
                },
                '.structurizrIcon': {
                }
            }
        });

        graph.addCell(cell);
        cell.toBack();
        mapOfIdToBox[element.id] = cell;

        cell._computedStyle = {};
        cell._computedStyle.background = configuration.background;
        cell._computedStyle.color = textColor;
        cell._computedStyle.borderStyle = configuration.border;
        cell._computedStyle.stroke = stroke;
        cell._computedStyle.fontSize = configuration.fontSize;
        cell._computedStyle.opacity = configuration.opacity;

        var cellView = paper.findViewByModel(cell);
        const widthOfName = $('#' + cellView.id + ' .structurizrName')[0].getComputedTextLength();
        const widthOfMetadata = $('#' + cellView.id + ' .structurizrMetaData')[0].getComputedTextLength();

        if (configuration.icon) {
            var iconRatio = getImageRatio(configuration.icon);
            var widthOfIcon = (heightOfIcon * iconRatio);

            cell.attributes.attrs['.structurizrIcon']['xlink:href'] = getImageMetadata(configuration.icon).dataURL;
            cell.attributes.attrs['.structurizrIcon']['width'] = widthOfIcon;
            cell.attributes.attrs['.structurizrIcon']['height'] = heightOfIcon;
            cell.attributes.attrs['.structurizrIcon']['opacity'] = (configuration.opacity/100);
            cell._computedStyle.icon = configuration.icon;

            cell._computedStyle.minimumWidth = widthOfIcon + 10 + Math.max(widthOfName, widthOfMetadata);
        } else {
            cell._computedStyle.minimumWidth = Math.max(widthOfName, widthOfMetadata);
        }

        const domElement = $('#' + cellView.id);
        domElement.attr('style', 'cursor: ' + (editable === true ? 'move' : 'default') + ' !important');

        if (editable === true) {
            domElement.attr('style', 'cursor: move !important');
        } else {
            domElement.attr('style', 'cursor: default !important');
        }

        if (navigationEnabled) {
            addDoubleClickHandlerForElement(cellView, element);
        }

        cell.on('change:position', moveLinksBetweenElementsContainedWithin);

        return cell;
    }

    function moveLinksBetweenElementsContainedWithin(cell, newPosition, opt) {
        if (opt.translateBy === undefined) {
            // cell has moved programmatically - ignore
        } else {
            const translatedByCell = graph.getCell(opt.translateBy);
            if (cell === translatedByCell) {
                // the change:position event handler for each element will take care of the element position,
                // but we still need to move all vertices inside this deployment node
                var dx = opt.tx;
                var dy = opt.ty;

                var embeddedCells = cell.getEmbeddedCells({ deep: true }).map(function(cell) { return paper.findViewByModel(cell); });
                moveLinksBetweenElements(embeddedCells, dx, dy);
            }
        }
    }

    function centreCell(cell) {
        const width = cell.get('size').width;
        const height = cell.get('size').height;
        const x = (diagramWidth - width) / 2;
        const y = (diagramHeight - height) / 2;

        cell.set({ position: { x: x, y: y }});
    }

    function centreCellHorizontally(cell) {
        const width = cell.get('size').width;
        const x = (diagramWidth - width) / 2;
        const y = cell.get('position').y;

        cell.set({ position: { x: x, y: y }});
    }

    this.setPaperSize = function(view) {
        if (view.dimensions === undefined) {
            view.dimensions = { width: 2000, height: 2000 };

            // no dimensions set: use legacy paper size
            if (view.paperSize !== undefined) {
                const dimensions = new structurizr.ui.PaperSizes().getDimensions(view.paperSize);
                if (dimensions) {
                    view.dimensions.width = dimensions.width;
                    view.dimensions.height = dimensions.height;
                }
            }
        } else {
            // check dimensions are valid
            if (view.dimensions.width < 500) {
                view.dimensions.width = 2000;
            }
            if (view.dimensions.height < 500) {
                view.dimensions.height = 2000;
            }
        }

        this.setPageSize(view.dimensions.width, view.dimensions.height);
    };

    function reposition(parentCell) {
        if (parentCell && parentCell.getEmbeddedCells().length > 0) {
            const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
            const metadataText = parentCell.attr('.structurizrMetaData').text;
            const fontSize = parentCell._computedStyle.fontSize;

            var defaultInternalPadding = '20';
            var internalPadding = clusterPadding;
            var margin = 15;

            // if (parentCell.elementInView === undefined) {
            //     internalPadding = parseInt(getViewOrViewSetProperty(viewOrFilter, 'structurizr.groupPadding', defaultInternalPadding));
            // } else {
            //     if (parentCell.elementInView.type === structurizr.constants.DEPLOYMENT_NODE_ELEMENT_TYPE) {
            //         internalPadding = parseInt(getViewOrViewSetProperty(viewOrFilter, 'structurizr.deploymentNodePadding', defaultInternalPadding));
            //     } else {
            //         internalPadding = parseInt(getViewOrViewSetProperty(viewOrFilter, 'structurizr.boundaryPadding', defaultInternalPadding));
            //     }
            // }

            const padding = { top: internalPadding, right: internalPadding, bottom: internalPadding + margin, left: internalPadding };

            var nameFontSize = parseInt(parentCell.attr('.structurizrName')['font-size']);
            var metadataFontSize = parseInt(parentCell.attr('.structurizrMetaData')['font-size']);

            if (metadataText && metadataText.length > 0) {
                padding.bottom = padding.bottom + nameFontSize + metadataFontSize + margin;
            } else {
                padding.bottom = padding.bottom + nameFontSize + margin;
            }

            var minX = Number.MAX_VALUE;
            var maxX = Number.MIN_VALUE;
            var minY = Number.MAX_VALUE;
            var maxY = Number.MIN_VALUE;

            var embeddedCells = parentCell.getEmbeddedCells();
            for (var i = 0; i < embeddedCells.length; i++) {
                var cell = embeddedCells[i];
                var x = cell.get('position').x;
                var y = cell.get('position').y;
                var width = cell.get('size').width;
                var height = cell.get('size').height;

                // if (cell.elementInView) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x + width);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y + height);
                // }
            }

            var newWidth = maxX - minX + padding.left + padding.right;
            var newHeight = maxY - minY + padding.top + padding.bottom;
            var newX = minX - padding.left;
            var newY = minY - padding.top;

            const minimumWidth = parentCell._computedStyle.minimumWidth;
            if (minimumWidth) {
                newWidth = Math.max(newWidth, minimumWidth + margin + margin);
            }

            var refX = (margin / newWidth);

            if (parentCell._computedStyle.icon !== undefined) {
                var iconWidth = parentCell.attr('.structurizrIcon')['width'];
                var iconHeight = parentCell.attr('.structurizrIcon')['height'];
                parentCell.attr({
                    '.structurizrIcon': {
                        'x': margin,
                        'y': newHeight - iconHeight - 10
                    }
                });

                refX = ((margin + 10 + iconWidth) / newWidth);
            }

            parentCell.position(newX, newY);
            parentCell.attr({ rect: { width: newWidth, height: newHeight }});
            parentCell.resize(newWidth, newHeight);

            if (metadataText && metadataText.length > 0) {
                parentCell.attr({
                    '.structurizrName': {
                        'ref-x': refX,
                        'y': newHeight - (15 + fontSize)
                    },
                    '.structurizrMetaData': {
                        'ref-x': refX,
                        'y': newHeight - 15
                    },
                    '.structurizrInstanceCount': {
                        'ref-x': (newWidth - margin) / newWidth,
                        'y': newHeight - 15
                    }
                });
            } else {
                parentCell.attr({
                    '.structurizrName': {
                        'ref-x': refX,
                        'y': newHeight - 15
                    },
                    '.structurizrMetaData': {
                        'ref-x': refX,
                        'y': newHeight - 15
                    },
                    '.structurizrInstanceCount': {
                        'ref-x': (newWidth - margin) / newWidth,
                        'y': newHeight - 15
                    }
                });
            }
        }
    }

    this.gatherElementsOnCanvas = function() {
        var previousPositions = [];

        // check and move all elements
        for (var i = 0; i < graph.getElements().length; i++) {
            var cell = graph.getElements()[i];
            var x = cell.get('position').x;
            var y = cell.get('position').y;
            var width = cell.get('size').width;
            var height = cell.get('size').height;

            var maxX = diagramWidth - width;
            var maxY = diagramHeight - height;

            if (x < 0 || x > maxX || y < 0 || y > maxY) {
                previousPositions.push(getCurrentElementPositions([paper.findViewByModel(cell)])[0]);
            }

            x = Math.min(Math.max(0, x), maxX);
            y = Math.min(Math.max(0, y), maxY);

            cell.set(
                {
                    position: {
                        x: x,
                        y: y
                    }
                }
            );
        }

        // and repeat for links
        var links = graph.getLinks();
        if (links) {
            links.forEach(function(link) {
                var oldVertices = link.get('vertices');
                var newVertices = [];
                var moved = false;
                if (oldVertices) {
                    oldVertices.forEach(function(vertex) {
                        var x = Math.min(Math.max(0, vertex.x), diagramWidth);
                        var y = Math.min(Math.max(0, vertex.y), diagramHeight);

                        if (x !== vertex.x || y !== vertex.y) {
                            moved = true;
                        }

                        newVertices.push({
                            x: x,
                            y: y
                        });
                    });

                    if (moved) {
                        previousPositions.push(getCurrentLinkPositions([link])[0]);
                        link.set('vertices', newVertices);
                    }
                }
            })
        }


        if (previousPositions.length > 0) {
            addToUndoBuffer(previousPositions);
        }
    };

    this.zoomFitWidth = function() {
        this.zoomTo((viewport.innerWidth()-scrollBarWidth) / diagramWidth);

        this.scrollToCentre();
    };

    this.zoomFitHeight = function() {
        this.zoomTo(viewport.innerHeight() / diagramHeight);

        this.scrollToCentre();
    };

    this.zoomFitContent = function() {
        if (currentView.type !== structurizr.constants.IMAGE_VIEW_TYPE && !currentView.elements) {
            this.zoomFitHeight();
            return;
        }

        var contentArea = findContentArea(true, 50);
        var contentWidth = contentArea.maxX - contentArea.minX;
        var contentHeight = contentArea.maxY - contentArea.minY;

        var viewportRatio = viewport.width() / viewport.height();
        var contentRatio = contentWidth / contentHeight;

        if (viewportRatio > contentRatio) {
            this.zoomTo(viewport.height() / contentHeight);
        } else {
            this.zoomTo(viewport.width() / contentWidth);
        }

        var viewportWidth = viewport.innerWidth();
        var viewportHeight = viewport.innerHeight();

        var viewpointCentreX = viewport.offset().left + (viewportWidth/2);
        var viewpointCentreY = viewport.offset().top + (viewportHeight/2);
        var clientTarget = { x: viewpointCentreX, y: viewpointCentreY };

        var centreX = contentArea.minX + ((contentArea.maxX - contentArea.minX)/2);
        var centreY = contentArea.minY + ((contentArea.maxY - contentArea.minY)/2);

        this.scrollToPoint(centreX, centreY, clientTarget.x, clientTarget.y);
    };

    function zoomFitElements(elements) {
        const contentArea = {
            minX: Number.MAX_VALUE,
            minY: Number.MAX_VALUE,
            maxX: 0,
            maxY: 0
        };

        const crop = true;
        const margin = 50;

        elements.forEach(function(elementId) {
            const cell = mapOfIdToBox[elementId];
            if (cell) {
                const bbox = paper.findViewByModel(cell).getBBox();
                contentArea.minX = Math.min(contentArea.minX, bbox.x);
                contentArea.minY = Math.min(contentArea.minY, bbox.y);

                contentArea.maxX = Math.max(contentArea.maxX, bbox.x + bbox.width);
                contentArea.maxY = Math.max(contentArea.maxY, bbox.y + bbox.height);
            }
        });

        contentArea.minX = contentArea.minX / scale;
        contentArea.maxX = contentArea.maxX / scale;
        contentArea.minY = contentArea.minY / scale;
        contentArea.maxY = contentArea.maxY / scale;

        if (crop === true) {
            contentArea.minX = Math.max(contentArea.minX - margin, 0);
            contentArea.maxX = Math.min(contentArea.maxX + margin, diagramWidth);
            contentArea.minY = Math.max(contentArea.minY - margin, 0);
            contentArea.maxY = Math.min(contentArea.maxY + margin, diagramHeight);
        }

        var contentWidth = contentArea.maxX - contentArea.minX;
        var contentHeight = contentArea.maxY - contentArea.minY;

        var viewportRatio = viewport.width() / viewport.height();
        var contentRatio = contentWidth / contentHeight;

        if (viewportRatio > contentRatio) {
            self.zoomTo(viewport.height() / contentHeight);
        } else {
            self.zoomTo(viewport.width() / contentWidth);
        }

        var viewportWidth = viewport.innerWidth();
        var viewportHeight = viewport.innerHeight();

        var viewpointCentreX = viewport.offset().left + (viewportWidth/2);
        var viewpointCentreY = viewport.offset().top + (viewportHeight/2);
        var clientTarget = { x: viewpointCentreX, y: viewpointCentreY };

        var centreX = contentArea.minX + ((contentArea.maxX - contentArea.minX)/2);
        var centreY = contentArea.minY + ((contentArea.maxY - contentArea.minY)/2);

        self.scrollToPointSmooth(centreX, centreY, clientTarget.x, clientTarget.y);
    }

    this.zoomIn = function(evt) {
        zoomToAndScroll(Math.min(scale + zoomDelta, maxZoomScale), evt);
    };

    this.zoomOut = function(evt) {
        zoomToAndScroll(Math.max(scale - zoomDelta, minZoomScale), evt);
    };

    function zoomToAndScroll(zoomScale, evt) {
        var viewportWidth = viewport.innerWidth();
        var viewportHeight = viewport.innerHeight();

        var clientTarget;
        if (evt !== undefined) {
            clientTarget = { x: evt.clientX, y: evt.clientY };
        } else {
            var viewpointCentreX = viewport.offset().left + (viewportWidth/2);
            var viewpointCentreY = viewport.offset().top + (viewportHeight/2);
            clientTarget = { x: viewpointCentreX, y: viewpointCentreY };
        }

        var localPointBeforeZooming = paper.clientToLocalPoint(clientTarget.x, clientTarget.y);

        self.zoomTo(zoomScale);
        self.scrollToPoint(localPointBeforeZooming.x, localPointBeforeZooming.y, clientTarget.x, clientTarget.y);
    }

    this.scrollToCentre = function() {
        var viewportWidth = viewport.innerWidth();
        var viewportHeight = viewport.innerHeight();

        var viewpointCentreX = viewport.offset().left + (viewportWidth/2);
        var viewpointCentreY = viewport.offset().top + (viewportHeight/2);
        var clientTarget = { x: viewpointCentreX, y: viewpointCentreY };

        var centreX = diagramWidth/2;
        var centreY = diagramHeight/2;

        this.scrollToPoint(centreX, centreY, clientTarget.x, clientTarget.y);
    }

    this.scrollToPoint = function(paperX, paperY, clientX, clientY) {
        const clientPoint = paper.localToClientPoint(paperX, paperY);
        const diffX = clientPoint.x - clientX;
        const diffY = clientPoint.y - clientY;

        viewport[0].scrollBy({
            top: diffY,
            left: diffX,
            behavior: 'instant'
        });
    };

    this.scrollToPointSmooth = function(paperX, paperY, clientX, clientY) {
        const clientPoint = paper.localToClientPoint(paperX, paperY);
        const diffX = clientPoint.x - clientX;
        const diffY = clientPoint.y - clientY;

        viewport[0].scrollBy({
            top: diffY,
            left: diffX,
            behavior: 'smooth'
        });
    };

    this.zoomTo = function(zoomScale) {
        if (zoomScale > maxZoomScale) {
            scale = maxZoomScale;
        } else {
            scale = zoomScale;
        }

        var width = Math.round(diagramWidth*scale);
        var height = Math.round(diagramHeight*scale);

        canvas.width(width);
        canvas.height(height);

        var svg = $("#v-2");
        svg.width(width);
        svg.height(height);

        if (width > viewport.innerWidth()) {
            viewport.css("overflow-x", "scroll");
        } else {
            viewport.css("overflow-x", "hidden");
        }
        if (height > viewport.innerHeight()) {
            viewport.css("overflow-y", "scroll");
        } else {
            viewport.css("overflow-y", "hidden");
        }

        paper.scale(scale);
    };

    this.getPossibleViewportWidth = function() {
        return parentElement.innerWidth();
    };

    this.getPossibleViewportHeight = function() {
        return parentElement.innerHeight();
    };

    this.zoomToWidthOrHeight = function() {
        var viewportRatio = viewport.innerWidth() / viewport.innerHeight();
        var diagramRatio = diagramWidth / diagramHeight;
        if (diagramRatio < viewportRatio) {
            this.zoomFitHeight();
        } else {
            this.zoomFitWidth();
        }

        setZoomAndMargins();
    };

    this.resize = function() {
        if (structurizr.ui.isFullScreen()) {
            if (this.isEditable()) {
                viewport.width($(window).width());
                viewport.height($(window).height() - $('#diagramControls').height());
            } else {
                viewport.width($(window).width());
                viewport.height($(window).height());
            }
        } else {
            viewport.width(this.getPossibleViewportWidth());

            if (!embedded) {
                var height = this.getPossibleViewportHeight();
                viewport.height(height);
                $('#diagramNavigationPanel').height(height);
            } else {
                var diagramRatio = diagramWidth / diagramHeight;
                var height = Math.floor(viewport.width() / diagramRatio);
                viewport.height(height);
                $('#diagramNavigationPanel').height(height);
            }
        }
    };

    this.onElementsSelected = function(callback) {
        elementsSelectedEventHandler = callback;
    }

    this.onElementDoubleClicked = function(callback) {
        elementDoubleClickedHandler = callback;
    }

    this.onRelationshipDoubleClicked = function(callback) {
        relationshipDoubleClickedHandler = callback;
    }

    function fireElementsSelectedEvent() {
        if (elementsSelectedEventHandler) {
            elementsSelectedEventHandler(selectedElements.map(function(cell) {
                return cell.model.elementInView.id;
            }));
        }
    }

    function selectElement(cellView) {
        cellView.selected = true;
        var structurizrBox = $('#' + cellView.el.id + ' .structurizrHighlightableElement');
        var classes = structurizrBox.attr('class');
        structurizrBox.attr('class', classes + ' highlightedElement');

        selectedElements.push(cellView);
        fireElementsSelectedEvent();
    }

    this.selectElementsWithName = function(regex) {
        var filter = new RegExp(regex);

        graph.getElements().forEach(function(element) {
            if (element.elementInView && element.positionCalculated === false) {
                var elementInModel = structurizr.workspace.findElementById(element.elementInView.id);
                if (elementInModel.name.match(filter)) {
                    var cellView = paper.findViewByModel(element);
                    selectElement(cellView);
                }
            }
        });
    };

    this.selectAllElements = function() {
        graph.getElements().forEach(function(element) {
            if (element.elementInView && element.positionCalculated === false) {
                var cellView = paper.findViewByModel(element);
                if (cellView.selected === undefined || cellView.selected === false) {
                    selectElement(cellView);
                }
            }
        });
    };

    this.deselectAllElements = function() {
        for (var i = selectedElements.length - 1; i >= 0; i--) {
            deselectElement(selectedElements[i]);
        }
    };

    function deselectElement(cellView) {
        cellView.selected = false;
        var structurizrBox = $('#' + cellView.el.id + ' .structurizrHighlightableElement');
        var classes = structurizrBox.attr('class');

        var highlightedElement = classes.indexOf(' highlightedElement');
        if (highlightedElement > -1) {
            structurizrBox.attr('class', classes.substring(0, highlightedElement));
        }

        var index = selectedElements.indexOf(cellView);
        if (index > -1) {
            selectedElements.splice(index, 1);
        }

        fireElementsSelectedEvent();
    }

    function createTagsList(style, defaultTag) {
        var tags = undefined;

        if (style.tags) {
            style.tags.forEach(function (tag) {
                if (tag !== defaultTag) {
                    if (!tags) {
                        tags = tag;
                    } else {
                        tags = tags + ", " + tag;
                    }
                }
            });
        }

        if (!tags) {
            tags = defaultTag;
        }

        return tags;
    }

    function getImageRatio(image) {
        return getImageMetadata(image).ratio;
    }

    function getImageMetadata(image) {
        return imageMetadata.filter(function(im) {
            return im.src === image;
        })[0];
    }

    function registerElementStyle(elementStyle) {
        const elementStyleIdentifier = createTagsList(elementStyle, "Element");
        elementStyle.id = elementStyleIdentifier;

        if (elementStylesInUse.indexOf(elementStyleIdentifier) === -1) {
            elementStylesInUse.push(elementStyleIdentifier);
            elementStylesInUseMap[elementStyleIdentifier] = elementStyle;
        }
    }

    function registerRelationshipStyle(relationshipStyle) {
        const relationshipStyleIdentifier = createTagsList(relationshipStyle, "Relationship");
        relationshipStyle.id = relationshipStyleIdentifier;

        if (relationshipStylesInUse.indexOf(relationshipStyleIdentifier) === -1) {
            relationshipStylesInUse.push(relationshipStyleIdentifier);
            relationshipStylesInUseMap[relationshipStyleIdentifier] = relationshipStyle;
        }
    }

    function createDiagramKey() {
        var keyElementWidth = 450;
        var keyElementHeight = 300;
        var fontSize = "30px";

        elementStylesInUse.sort(function(a, b){ return a.localeCompare(b); });
        relationshipStylesInUse.sort(function(a, b){ return a.localeCompare(b); });

        var numberOfItemsInKey = elementStylesInUse.length + relationshipStylesInUse.length;
        if (currentView.type === "Deployment") {
            numberOfItemsInKey++;
        }
        const columns = 5;
        const columnWidth = 500;
        const rowHeight = 500;
        const rows = Math.ceil(numberOfItemsInKey / columns);
        totalWidthOfKey = columns * columnWidth;
        totalHeightOfKey = rows * rowHeight;
        var counter = 1;

        var svg = '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" width="' + totalWidthOfKey + '" height="' + totalHeightOfKey + '" style="padding: 10px; font-size: ' + fontSize + '; font-family: ' + font.name + '; background: ' + canvasColor + ';">';

        for (var i = 0; i < elementStylesInUse.length; i++) {
            elementStyle = elementStylesInUseMap[elementStylesInUse[i]];
            var fill = structurizr.util.shadeColor(elementStyle.background, 100-elementStyle.opacity, darkMode);
            var stroke = (elementStyle.stroke !== undefined ? structurizr.util.shadeColor(elementStyle.stroke, 100-elementStyle.opacity, darkMode) : structurizr.util.shadeColor(fill, darkenPercentage, darkMode));
            var strokeWidth = elementStyle.strokeWidth;
            const strokeDashArray = dashArrayForElement(elementStyle);

            var textColor = structurizr.util.shadeColor(elementStyle.color, 100-elementStyle.opacity, darkMode);

            if (elementStyle.shape === "RoundedBox") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="20" ry="20" x="0" y="0" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Cylinder") {
                var lidRadius = 45;
                var uniqueKey = "key" + elementStyle.tag.replace(/ /g, "") + "Cylinder";
                var width = keyElementWidth;
                var height = keyElementWidth * (elementStyle.height / elementStyle.width);

                var path = 'M 0,' + (lidRadius/2);
                path += ' a ' + (width / 2) + ',' + (lidRadius/2) + ' 0,0,0 ' + width + ' ' + 0;
                path += ' a ' + (width / 2) + ',' + (lidRadius/2) + ' 0,0,0 -' + width + ' ' + 0;
                path += ' l 0,' + (height - lidRadius);
                path += ' a ' + (width / 2) + ',' + (lidRadius/2) + ' 0,0,0 ' + width + ' ' + 0;
                path += ' l 0,-' + (height - lidRadius);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<path id="' + uniqueKey.concat("Path") + '" d="' + path + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"></path>';
                svg += createTextForKey(width, height, 0, lidRadius, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Bucket") {
                var lidRadius = 45;
                var uniqueKey = "key" + elementStyle.tag.replace(/ /g, "") + "Cylinder";
                var width = keyElementWidth;
                var height = keyElementWidth * (elementStyle.height / elementStyle.width);

                var path = 'M 0,' + (lidRadius/2);
                path += ' a ' + (width / 2) + ',' + (lidRadius/2) + ' 0,0,0 ' + width + ' ' + 0;
                path += ' a ' + (width / 2) + ',' + (lidRadius/2) + ' 0,0,0 -' + width + ' ' + 0;
                path += ' l ' + (width/10) + ',' + (height - lidRadius);
                path += ' a ' + (width / 2) + ',' + lidRadius + ' 0,0,0 ' + (width - (width*0.2)) + ' ' + 0;
                path += ' l ' + (width/10) + ',-' + (height - lidRadius);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<path id="' + uniqueKey.concat("Path") + '" d="' + path + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"></path>';
                svg += createTextForKey(width, height, 0, lidRadius, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Pipe") {
                var lidRadius = 45;
                var uniqueKey = "key" + elementStyle.tag.replace(/ /g, "") + "Pipe";
                var width = keyElementWidth;
                var height = keyElementHeight;

                var path = 'M ' + (lidRadius/2) + ',0';
                path += ' a ' + (lidRadius/2) + ',' + (height / 2) + ' 0,0,1 0 ' + height;
                path += ' a ' + (lidRadius/2) + ',' + (height / 2) + ' 0,0,1 0 -' + height;
                path += ' l ' + (width - lidRadius) + ',0';
                path += ' a ' + (lidRadius/2) + ',' + (height / 2) + ' 0,0,1 0 ' + height;
                path += ' l -' + (width - lidRadius) + ',0';

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<path id="' + uniqueKey.concat("Path") + '" d="' + path + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"></path>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Person") {
                var width = keyElementWidth;
                var height = keyElementWidth;
                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect x="0" y="' + height/2.5 + '" width="' + width + '" height="' + (height - (height/2.5)) + '" rx="90" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<circle cx="' + width/2 + '" cy="' + height/4.5 + '" r="' + height/4.5 + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<line x1="' + width/5 + '" y1="' + height/1.5 + '" x2="' + width/5 + '" y2="' + height + '" stroke-width="1" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<line x1="' + (width-(width/5)) + '" y1="' + height/1.5 + '" x2="' + (width-(width/5)) + '" y2="' + height + '" stroke-width="1" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, height/2.5, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Robot") {
                var width = keyElementWidth;
                var height = keyElementWidth;
                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect x="0" y="' + height/2.5 + '" width="' + width + '" height="' + (height - (height/2.5)) + '" rx="40" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect x="' + (width - width/1.8)/2 + '" y="' + (width/2.25 - width/10)/2 + '" width="' + width/1.8 + '" height="' + height/10 + '" rx="10" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect x="' + (height - height/2.25)/2 + '" y="0" width="' + width/2.25 + '" height="' + height/2.25 + '" rx="40" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<line x1="' + width/5 + '" y1="' + height/1.5 + '" x2="' + width/5 + '" y2="' + height + '" stroke-width="1" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<line x1="' + (width-(width/5)) + '" y1="' + height/1.5 + '" x2="' + (width-(width/5)) + '" y2="' + height + '" stroke-width="1" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, height/2.5, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Folder") {
                var width = keyElementWidth;
                var height = keyElementWidth * (elementStyle.height / elementStyle.width);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + (width / 3) + '" height="' + (height / 4) + '" rx="15" ry="15" x="15" y="0" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + width + '" height="' + (height - (height / 8)) + '" rx="6" ry="6" x="0" y="' + (height / 8) + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, height / 8, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Component") {
                var width = keyElementWidth;
                var height = keyElementHeight;
                var blockWidth = keyElementWidth / 6;
                var blockHeight = keyElementHeight / 8;
                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + (width - (blockWidth / 2)) + '" height="' + height + '" rx="10" ry="10" x="' + (blockWidth / 2) + '" y="0" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + blockWidth + '" height="' + blockHeight + '" rx="5" ry="5" x="0" y="' + (blockHeight * 0.6) + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + blockWidth + '" height="' + blockHeight + '" rx="5" ry="5" x="0" y="' + (blockHeight * 2) + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey((width - (blockWidth / 2)), height, (blockWidth / 2), 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Circle") {
                var width = keyElementWidth;
                var height = keyElementWidth;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<ellipse cx="' + width/2 + '" cy="' + height/2 + '" rx="' + width/2 + '" ry="' + height/2 + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Ellipse") {
                var width = keyElementWidth;
                var height = keyElementWidth * (elementStyle.height / elementStyle.width);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<ellipse cx="' + width/2 + '" cy="' + height/2 + '" rx="' + width/2 + '" ry="' + height/2 + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Hexagon") {
                var width = keyElementWidth;
                var height = Math.floor(((width/2) * Math.sqrt(3)));
                var points =    (width/4) + ",0 " +
                    (3*(width/4)) + ",0 " +
                    width + "," + (height/2) + " " +
                    (3*(width/4)) + "," + height + " " +
                    (width/4) + "," + height + " " +
                    "0," + (height/2);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<polygon points="' + points + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Diamond") {
                var width = keyElementWidth;
                var height = keyElementHeight;
                var points =    (width/2) + ",0 " +
                    width + "," + (height/2) + " " +
                    (width/2) + "," + height + " " +
                    "0," + (height/2);

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<polygon points="' + points + '" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "WebBrowser") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="10" ry="10" x="0" y="0" fill="' + stroke + '" stroke-width="0" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + (width-strokeWidth*2) + '" height="' + (height-40-strokeWidth) + '" rx="10" ry="10" x="' + strokeWidth + '" y="40" fill="' + fill + '" stroke-width="0"/>';
                svg += '<rect width="' + (width-110) + '" height="20" rx="10" ry="10" x="100" y="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="20" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="50" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="80" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += createTextForKey(width, height, 0, 40, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Window") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="10" ry="10" x="0" y="0" fill="' + stroke + '" stroke-width="0" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + (width-strokeWidth*2) + '" height="' + (height-40-strokeWidth) + '" rx="10" ry="10" x="' + strokeWidth + '" y="40" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="20" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="50" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="80" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += createTextForKey(width, height, 0, 40, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Terminal") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="10" ry="10" x="0" y="0" fill="' + stroke + '" stroke-width="0" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + (width-strokeWidth*2) + '" height="' + (height-40-strokeWidth) + '" rx="10" ry="10" x="' + strokeWidth + '" y="40" fill="' + fill + '" stroke-width="0"/>';
                svg += '<text x="50" y="90" text-anchor="middle" fill="' + stroke + '" font-size="50px" font-family="Courier New, Arial" font-weight="bold">';
                svg += '<tspan>>_</tspan>';
                svg += '</text>';
                svg += '<ellipse cx="20" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="50" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="80" cy="20" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += createTextForKey(width, height, 0, 40, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "Shell") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="10" ry="10" x="0" y="0" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<text x="50" y="50" text-anchor="middle" fill="' + stroke + '" font-size="50px" font-family="Courier New, Arial" font-weight="bold">';
                svg += '<tspan>>_</tspan>';
                svg += '</text>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "MobileDevicePortrait") {
                var width = keyElementHeight;
                var height = keyElementWidth;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="20" ry="20" x="0" y="0" fill="' + stroke + '" stroke-width="5" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + (width-20) + '" height="' + (height-80) + '" rx="5" ry="5" x="10" y="40" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="' + (width/2) + '" cy="' + (height-20) + '" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<line x1="' + ((width-50)/2) + '" y1="20" x2="' + (width-((width-50)/2)) + '" y2="20" stroke-width="5" stroke="' + fill + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else if (elementStyle.shape === "MobileDeviceLandscape") {
                var width = keyElementWidth;
                var height = keyElementHeight;

                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="20" ry="20" x="0" y="0" fill="' + stroke + '" stroke-width="5" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += '<rect width="' + (width-80) + '" height="' + (height-20) + '" rx="5" ry="5" x="40" y="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<ellipse cx="20" cy="' + (height/2) + '" rx="10" ry="10" fill="' + fill + '" stroke-width="0"/>';
                svg += '<line x1="' + (width-20) + '" y1="' + ((height-50)/2) + '" x2="' + (width-20) + '" y2="' + (height - ((height-50)/2)) + '" stroke-width="5" stroke="' + fill + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            } else {
                var cornerRadius = 3;

                if (elementStyle.tags.indexOf('Deployment Node') > -1) {
                    cornerRadius = 15;
                }

                var width = keyElementWidth;
                var height = keyElementHeight;
                svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
                svg += '<rect width="' + width + '" height="' + height + '" rx="' + cornerRadius + '" ry="' + cornerRadius + '" x="0" y="0" fill="' + fill + '" stroke-width="' + strokeWidth + '" stroke="' + stroke + '" stroke-dasharray="' + strokeDashArray + '"/>';
                svg += createTextForKey(width, height, 0, 0, elementStyle.id, undefined, textColor, elementStyle.icon, elementStyle.opacity);
                svg += '</g>';
            }

            counter++;
        }

        for (var i = 0; i < relationshipStylesInUse.length; i++) {
            relationshipStyle = relationshipStylesInUseMap[relationshipStylesInUse[i]];
            const strokeDashArray = dashArrayForRelationship(relationshipStyle);

            var fill = structurizr.util.shadeColor(relationshipStyle.color, 100-relationshipStyle.opacity, darkMode);

            var width = keyElementWidth;
            var height = 160;
            const arrowSize = 20 + (relationshipStyle.thickness * 4);
            svg += createSvgGroup(counter, columns, columnWidth, rowHeight, width, height);
            svg += '<path d="M' + (width-arrowSize) + ',0 L' + (width-arrowSize) + ',' + (30 + (arrowSize/2)) + ' L' + width + ',30 L ' + (width-arrowSize) + ',' + (30 - (arrowSize/2)) + '" style="fill:' + fill + '" />';
            svg += '<path d="M0,30 L' + (width-arrowSize) + ',30" style="stroke:' + fill + '; stroke-width: ' + relationshipStyle.thickness + '; fill: none; stroke-dasharray: ' + strokeDashArray + ';" />';
            svg += createTextForKey(width, height, 0, 60, relationshipStyle.id, undefined, fill);
            svg += '</g>';

            counter++;
        }

        svg += '</svg>';

        return svg;
    }

    function createSvgGroup(index, columns, columnWidth, rowHeight, contentWidth, contentHeight) {
        var column = ((index-1) % columns) + 1;
        var row = Math.ceil(index / columns);
        var offsetX = ((column-1) * columnWidth) + ((columnWidth - contentWidth)/2);
        var offsetY = ((row-1) * rowHeight) + ((rowHeight - contentHeight)/2);

        return '<g transform="translate(' + offsetX +',' + offsetY + ')">';
    }

    function createTextForKey(width, height, offsetX, offsetY, tag, stereotype, textColor, icon, opacity) {
        var fontSize = 30;
        var heightOfIcon = 60;
        const paddingBetweenTextAndIcon = fontSize;
        var text = breakText(structurizr.util.escapeHtml(tag), width * 0.8, font.name, fontSize);

        var heightOfText = calculateHeight(text, fontSize, 0);

        var textX = (offsetX + (width / 2));
        var textY;
        var iconY;

        if (icon === undefined) {
            textY = ((height - offsetY - heightOfText)/2) + offsetY;
        } else {
            textY = ((height - offsetY - heightOfText - paddingBetweenTextAndIcon - heightOfIcon)/2) + offsetY;
            iconY = textY + heightOfText + paddingBetweenTextAndIcon;
        }

        textY -= (fontSize + 10);

        var svg = "";
        svg += '<text x="' + textX + '" y="' + textY + '" text-anchor="middle" dominant-baseline="hanging" fill="' + textColor + '" font-size="' + fontSize + 'px" font-family="' + font.name + '">';

        text.split("\n").forEach(function(line) {
            svg += '<tspan x="' + textX + '" dy="' + (fontSize + 10) + 'px">' + line + '</tspan>';
        });

        svg += '</text>';

        if (icon) {
            var iconWidth = (getImageRatio(icon) * heightOfIcon);
            svg += '<image xlink:href="' + getImageMetadata(icon).dataURL + '" x="' + (offsetX + ((width-iconWidth)/2)) + '" y="' + iconY + '" width="' + iconWidth + '" height="' + heightOfIcon + '" opacity="' + (opacity ? (opacity/100) : 1) + '"/>';
        }

        return svg;
    }

    function getSvgOfCurrentDiagram() {
        var svgMarkup = canvas.html();
        svgMarkup = svgMarkup.substr(svgMarkup.indexOf('<svg'));

        return svgMarkup;
    }

    function findContentArea(crop, margin) {
        var minX = diagramWidth;
        var maxX = 0;
        var minY = diagramHeight;
        var maxY = 0;

        for (var i = 0; i < graph.getElements().length; i++) {
            var cell = graph.getElements()[i];

            if (cell.elementInView !== undefined || cell.attributes.type === 'structurizr.boundary' || cell.attributes.type === 'structurizr.image') {
                var bbox = paper.findViewByModel(cell).getBBox();
                minX = Math.min(minX, bbox.x);
                minY = Math.min(minY, bbox.y);

                maxX = Math.max(maxX, bbox.x + bbox.width);
                maxY = Math.max(maxY, bbox.y + bbox.height);
            }
        }

        for (var i = 0; i < graph.getLinks().length; i++) {
            var link = graph.getLinks()[i];
            var bbox = paper.findViewByModel(link).getBBox();

            minX = Math.min(minX, bbox.x);
            minY = Math.min(minY, bbox.y);

            maxX = Math.max(maxX, bbox.x + bbox.width);
            maxY = Math.max(maxY, bbox.y + bbox.height);
        }

        minX = minX / scale;
        maxX = maxX / scale;
        minY = minY / scale;
        maxY = maxY / scale;

        if (crop === true) {
            minX = Math.max(minX - margin, 0);
            maxX = Math.min(maxX + margin, diagramWidth);
            minY = Math.max(minY - margin, 0);
            maxY = Math.min(maxY + margin, diagramHeight);
        }

        return {
            minX: minX,
            maxX: maxX,
            minY: minY,
            maxY: maxY
        };
    }

    function convertDiagramToPNG(options, callback) {
        const svg = self.exportCurrentDiagramToSVG(options);

        return svgToPng(svg.markup, svg.width, svg.height, callback);
    }

    function svgToPng(svgMarkup, width, height, callback) {
        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        const img = new Image();
        img.onload = function() {
            const ctx = canvas.getContext("2d");
            ctx.drawImage(img, 0, 0);
            try {
                const png = canvas.toDataURL(structurizr.constants.CONTENT_TYPE_IMAGE_PNG);
                callback(png);
            } catch (e) {
                console.log(e);
            }
        };

        img.src = 'data:image/svg+xml;base64,' + structurizr.util.btoa(svgMarkup);
    }


    this.exportCurrentDiagramToSVG = function(options, callback) {
        if (options === undefined) {
            options = {
                metadata: true,
                crop: false
            }
        }

        const includeDiagramMetadata = options.metadata;
        const crop = options.crop;

        var width = diagramWidth;
        var height = diagramHeight;

        var currentScale = scale;
        this.zoomTo(1.0);

        if (!includeDiagramMetadata) {
            $(".structurizrMetadata>tspan").attr('display', 'none');
        }

        $(".structurizrNavigation").attr('display', 'none');

        var svgMarkup = getSvgOfCurrentDiagram();
        svgMarkup = svgMarkup.substring(svgMarkup.indexOf(">") +1 );

        svgMarkup = '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" viewBox="0 0 ' + diagramWidth + ' ' + diagramHeight + '" style="background: ' + canvasColor + '">' + svgMarkup;

        // remove some cursor definitions (leave the pointer and zoom-in cursors)
        svgMarkup = svgMarkup.replace(/cursor: move !important/g, '');
        svgMarkup = svgMarkup.replace(/class="joint-tools-layer"/g, 'class="joint-tools-layer" display="none"');

        svgMarkup = svgMarkup.replace(/class="[\w -]*"/g, '');
        svgMarkup = svgMarkup.replace(/data-type="[\w.]*"/g, '');
        svgMarkup = svgMarkup.replace(/model-id="[\w\d-]*"/g, '');

        svgMarkup = svgMarkup.replace(/cursor: default !important/g, '');
        svgMarkup = svgMarkup.replace(/cursor: pointer !important/g, '');
        svgMarkup = svgMarkup.replace(/cursor: zoom-in !important/g, '');

        svgMarkup = svgMarkup.replace(/&nbsp;/g, ' ');

        this.zoomTo(currentScale);
        $(".structurizrNavigation").attr('display', 'block');
        $(".structurizrMetadata>tspan").attr('display', 'block');

        if (crop) {
            var contentArea;
            if (crop === true) {
                contentArea = findContentArea(true, 50);
            } else {
                contentArea = {
                    minX: 0,
                    minY: 0,
                    maxX: diagramWidth,
                    maxY: diagramHeight
                }
            }

            width = contentArea.maxX - contentArea.minX;
            height = contentArea.maxY - contentArea.minY;

            const viewbox = ' viewBox="' + contentArea.minX + " " + contentArea.minY + " " + width + " " + height + '"';
            const svgOpeningTag = '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" width="' + width +'" height="' + height + '" style="width: ' + width + 'px; height: ' + height + 'px; background: ' + canvasColor + '"' + viewbox + '>';

            // replace opening tag with dimensions (some browsers seem to require this)
            svgMarkup = svgOpeningTag + svgMarkup.substring(svgMarkup.indexOf('>') + 1, svgMarkup.length);

            // this hides the handles used to change vertices
            svgMarkup = svgMarkup.replace(/class="marker-vertices"/g, 'class="marker-vertices" display="none"');

            // and remove the &nbsp; added by JointJS (otherwise you get a blank PNG file)
            svgMarkup = svgMarkup.replace(/&nbsp;/g, ' ');

            // remove any control characters (these shouldn't be there anyway, but...)
            svgMarkup = svgMarkup.replace(/[\x00-\x19]+/g, "");
        }

        const result = {
            markup: svgMarkup,
            width: width,
            height: height
        };

        if (callback !== undefined) {
            callback(result.markup);
        } else {
            return result;
        }
    };

    this.exportCurrentDiagramKeyToSVG = function(callback) {
        var svgMarkup = diagramKey;

        var diagramKeyWidth = svgMarkup.match(/width="(\d*)"/)[1];
        var diagramKeyHeight = svgMarkup.match(/height="(\d*)"/)[1];

        svgMarkup = svgMarkup.substring(svgMarkup.indexOf(">") +1 );
        svgMarkup = '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" viewBox="0 0 ' + diagramKeyWidth + ' ' + diagramKeyHeight + '" style="background: ' + canvasColor + '">' + svgMarkup;

        if (callback !== undefined) {
            callback(svgMarkup);
        } else {
            return svgMarkup;
        }
    };

    function resizeImage(url, width, height, callback) {
        var sourceImage = new Image();

        sourceImage.onload = function() {
            var canvas = document.createElement("canvas");
            canvas.width = width;
            canvas.height = height;
            canvas.getContext("2d").drawImage(sourceImage, 0, 0, width, height);
            callback(canvas.toDataURL());
        };

        sourceImage.src = url;
    }

    this.exportCurrentDiagram = function(options, callback) {
        if (options.format === 'png') {
            return this.exportCurrentDiagramToPNG(options, callback);
        } else {
            return this.exportCurrentDiagramToSVG(options, callback);
        }
    };

    this.exportCurrentDiagramToPNG = function(options, callback) {
        var currentScale = scale;
        this.zoomTo(1.0);
        $(".structurizrNavigation").attr('display', 'none');

        convertDiagramToPNG(options, function(png) {
            $(".structurizrNavigation").attr('display', 'block');
            self.zoomTo(currentScale);
            callback(png);
        });
    };

    this.exportCurrentDiagramKey = function(options, callback) {
        if (currentView.type === structurizr.constants.IMAGE_VIEW_TYPE) {
            return callback(undefined);
        }

        if (options.format === 'png') {
            return this.exportCurrentDiagramKeyToPNG(callback);
        } else {
            return this.exportCurrentDiagramKeyToSVG(callback);
        }
    };

    this.exportCurrentDiagramKeyToPNG = function(callback) {
        if (currentView.type === structurizr.constants.IMAGE_VIEW_TYPE) {
            return callback(undefined);
        }

        return svgToPng(diagramKey, totalWidthOfKey, totalHeightOfKey, callback);
    };

    this.exportCurrentThumbnailToPNG = function(callback) {
        try {
            convertDiagramToPNG({ metadata: true, crop: false }, function(exportedImage) {
                $(".structurizrNavigation").attr('display', 'block');
                resizeImage(exportedImage, thumbnailWidth, Math.floor(diagramHeight / (diagramWidth / thumbnailWidth)), callback);
            });
        } catch (err) {
            console.log(err);
        }
    };

    this.currentViewIsDynamic = function() {
        return currentView.type === structurizr.constants.DYNAMIC_VIEW_TYPE;
    };

    this.currentViewIsImage = function() {
        return currentView.type === structurizr.constants.IMAGE_VIEW_TYPE;
    };

    this.currentViewHasAnimation = function() {
        return currentView.animations !== undefined && currentView.animations.length > 1;
    };

    this.animationStarted = function() {
        return animationStarted;
    };

    this.onAnimationStarted = function(f) {
        animationStartedEventHandler = f;
    };

    this.onAnimationStopped = function(f) {
        animationStoppedEventHandler = f;
    };

    function fireAnimationStartedEvent() {
        if (animationStartedEventHandler) {
            animationStartedEventHandler();
        }
    }

    function fireAnimationStoppedEvent() {
        if (animationStoppedEventHandler) {
            animationStoppedEventHandler();
        }
    }

    this.startAnimation = function(autoPlay) {
        hideAllElements(1.0);
        hideAllLines(1.0);
        unfadeAllElements();

        if (this.currentViewIsDynamic()) {
            animationStarted = true;
            fireAnimationStartedEvent();
            linesToAnimate = graph.getLinks();
            linesToAnimate.sort(function (a, b) {
                return a.relationshipInView.order - b.relationshipInView.order;
            });
            animationIndex = 0;

            this.continueAnimation(autoPlay);
        } else if (this.currentViewHasAnimation()) {
            animationStarted = true;
            fireAnimationStartedEvent();
            animationSteps = currentView.animations;
            animationSteps.sort(function (a, b) {
                return a.order - b.order;
            });
            animationIndex = 0;

            this.continueAnimation(autoPlay);
        }
    };

    this.stepBackwardInAnimation = function() {
        if (this.currentViewIsDynamic()) {
            if (this.animationStarted()) {

                if (animationIndex > 0) {
                    animationIndex--;

                    // go back and find the previous order ID
                    var order = linesToAnimate[animationIndex].relationshipInView.order;
                    while (animationIndex >= 0 && linesToAnimate[animationIndex].relationshipInView.order === order) {
                        animationIndex--;
                    }

                    if (animationIndex > 0) {
                        order = linesToAnimate[animationIndex].relationshipInView.order;
                        while (animationIndex >= 0 && linesToAnimate[animationIndex].relationshipInView.order === order) {
                            animationIndex--;
                        }
                        animationIndex++;
                    }
                }

                if (animationIndex >= 0) {
                    this.continueAnimation(false);
                } else {
                    this.stopAnimation();
                }
            }
        } else if (this.currentViewHasAnimation()) {
            if (this.animationStarted()) {
                if (animationIndex === 1) {
                    this.stopAnimation();
                } else if (animationIndex > 1) {
                    animationIndex--;

                    var animationStep = animationSteps[animationIndex];
                    if (animationStep) {
                        if (animationStep.elements) {
                            animationStep.elements.forEach(function (elementId) {
                                hideElement(elementId, "0.0");
                            });
                        }
                        if (animationStep.relationships) {
                            animationStep.relationships.forEach(function(relationshipId) {
                                hideRelationship(relationshipId, "0.0");
                            });
                        }
                    }

                    animationIndex--;
                    this.continueAnimation(false);
                }
            }
        }
    };

    this.stepForwardInAnimation = function() {
        if (!this.animationStarted()) {
            this.startAnimation(false);
        } else {
            this.continueAnimation(false);
        }
    };

    this.continueAnimation = function(autoPlay) {
        if (this.currentViewIsDynamic()) {
            if (linesToAnimate) {
                hideAllLines('0.2');
                fadeAllElements();

                if (animationIndex < linesToAnimate.length) {
                    var line = linesToAnimate[animationIndex];

                    highlightLinesWithOrder(line.relationshipInView.order);

                    if (autoPlay) {
                        setTimeout(function () {
                            self.continueAnimation(true);
                        }, animationDelay);
                    }
                } else {
                    this.stopAnimation();
                }
            }
        } else if (this.currentViewHasAnimation()) {
            if (animationIndex === 0) {
                hideAllLines('0.0');
                hideAllElements('0.0');
            }

            var animationStep = animationSteps[animationIndex];
            if (animationStep) {
                if (animationStep.elements) {
                    animationStep.elements.forEach(function (elementId) {
                        showElement(elementId);
                    });
                }
                if (animationStep.relationships) {
                    animationStep.relationships.forEach(function(relationshipId) {
                        showRelationship(relationshipId);
                    });
                }
            }

            if (animationIndex < (animationSteps.length - 1)) {
                animationIndex++;

                if (autoPlay) {
                    setTimeout(function () {
                        self.continueAnimation(true);
                    }, animationDelay);
                }
            } else {
                this.stopAnimation();
            }
        }
    };

    function highlightRelationship(cellView) {
        $('#' + cellView.id).children().first().css({
            'stroke': '#aaaaaa',
            'stroke-width': 15
        });
    }

    function unhighlightRelationship(cellView) {
        $('#' + cellView.id).children().first().css({
            'stroke': '',
            'stroke-width': 10
        });
    }

    function hideAllLines(opacity) {
        $('.joint-link').css('opacity', opacity);
    }

    function hideAllElements(opacity) {
        $('.structurizrElement').css('opacity', opacity);
    }

    function fadeAllElements() {
        var shadePercentage = 80;

        cells.forEach(function(cell) {
            var background = structurizr.util.shadeColor(cell._computedStyle.background, shadePercentage, darkMode);
            var color = structurizr.util.shadeColor(cell._computedStyle.color, shadePercentage, darkMode);
            var stroke = structurizr.util.shadeColor(cell._computedStyle.stroke, shadePercentage, darkMode);
            changeColourOfCell(cell, background, color, stroke);

            // and make any icons transparent
            var domId = paper.findViewByModel(cell).id;
            $('#' + domId + ' .structurizrIcon').css('opacity', '0.2');
        });
    }

    function showRelationship(relationshipId, order) {
        Object.keys(mapOfIdToLine).forEach(function(id) {
            var line;
            if (order) {
                if (id === relationshipId + '/' + order) {
                    line = mapOfIdToLine[id];
                }
            } else {
                if (id === relationshipId || id.split('/')[0] === relationshipId) {
                    line = mapOfIdToLine[id];
                }
            }

            if (line) {
                var lineView = paper.findViewByModel(line);
                if (lineView) {
                    $('#' + lineView.el.id).css('opacity', '1.0');
                }
            }
        });
    }

    function hideRelationship(relationshipId, opacity) {
        Object.keys(mapOfIdToLine).forEach(function(id) {
            if (id === relationshipId || id.split('/')[0] === relationshipId) {
                var line = mapOfIdToLine[id];
                if (line) {
                    var lineView = paper.findViewByModel(line);
                    if (lineView) {
                        $('#' + lineView.el.id).css('opacity', opacity);
                    }
                }
            }
        });
    }

    function showElement(elementId) {
        var element = mapOfIdToBox[elementId];
        if (element) {
            var elementView = paper.findViewByModel(element);
            $('#' + elementView.el.id + ' .structurizrElement').css('opacity', '1.0');
        }
    }

    function unfadeAllElements() {
        Object.keys(mapOfIdToBox).forEach(function(elementId) {
            unfadeElement(elementId);
        });
    }

    function unfadeElement(elementId) {
        var cell = mapOfIdToBox[elementId];
        if (cell) {
            var background = cell._computedStyle.background;
            var foreground = cell._computedStyle.color;
            var stroke = cell._computedStyle.stroke;
            var opacity = cell._computedStyle.opacity;
            changeColourOfCell(cell, background, foreground, stroke);

            // and make any icons opaque
            var domId = paper.findViewByModel(cell).id;
            $('#' + domId + ' .structurizrIcon').css('opacity', opacity/100);
        }
    }

    this.changeColorOfElement = function(elementId, background) {
        var cell = mapOfIdToBox[elementId];
        if (cell) {
            var foreground = cell._computedStyle.color;
            var stroke = structurizr.util.shadeColor(background, darkenPercentage, darkMode);

            changeColourOfCell(cell, background, foreground, stroke);
        }
    };

    function changeColourOfCell(cell, background, color, stroke) {
        var type = cell.attributes.type;
        var domId = paper.findViewByModel(cell).id;

        if (type === "structurizr.box") {
            var selector = $('#' + domId + ' .structurizrBox');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.person") {
            var selector = $('#' + domId + ' .structurizrPersonHead');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrPersonBody');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrPersonLeftArm');
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrPersonRightArm');
            selector.css('stroke', stroke);
        } else if (type === "structurizr.robot") {
            var selector = $('#' + domId + ' .structurizrRobotHead');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrRobotBody');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrRobotEars');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrRobotLeftArm');
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrRobotRightArm');
            selector.css('stroke', stroke);
        } else if (type === "structurizr.cylinder") {
            var selector = $('#' + domId + ' .structurizrCylinderPath');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.pipe") {
            var selector = $('#' + domId + ' .structurizrPipePath');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.folder") {
            var selector = $('#' + domId + ' .structurizrFolderBody');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrFolderTab');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.component") {
            var selector = $('#' + domId + ' .structurizrComponent');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrComponentBlockTop');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrComponentBlockBottom');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.ellipse") {
            var selector = $('#' + domId + ' .structurizrEllipse');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.hexagon") {
            var selector = $('#' + domId + ' .structurizrHexagon');
            selector.css('fill', background);
            selector.css('stroke', stroke);
        } else if (type === "structurizr.webBrowser") {
            var selector = $('#' + domId + ' .structurizrWebBrowser');
            selector.css('fill', stroke);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrWebBrowserPanel');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWebBrowserUrlBar');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWebBrowserButton1');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWebBrowserButton2');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWebBrowserButton3');
            selector.css('fill', background);
        } else if (type === "structurizr.window") {
            var selector = $('#' + domId + ' .structurizrWindow');
            selector.css('fill', stroke);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrWindowPanel');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWindowButton1');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWindowButton2');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrWindowButton3');
            selector.css('fill', background);
        } else if (type === "structurizr.terminal") {
            var selector = $('#' + domId + ' .structurizrTerminal');
            selector.css('fill', stroke);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrTerminalPanel');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrTerminalButton1');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrTerminalButton2');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrTerminalButton3');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrTerminalPrompt');
            selector.css('fill', stroke);
        } else if (type === "structurizr.shell") {
            var selector = $('#' + domId + ' .structurizrShell');
            selector.css('fill', background);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrShellPrompt');
            selector.css('fill', stroke);
        } else if (type === "structurizr.mobileDevice") {
            var selector = $('#' + domId + ' .structurizrMobileDevice');
            selector.css('fill', stroke);
            selector.css('stroke', stroke);

            selector = $('#' + domId + ' .structurizrMobileDeviceDisplay');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrMobileDeviceButton');
            selector.css('fill', background);

            selector = $('#' + domId + ' .structurizrMobileDeviceSpeaker');
            selector.css('stroke', background);
        } else if (type === "structurizr.deploymentNode") {
            var selector = $('#' + domId + ' .structurizrDeploymentNode');
            selector.css('stroke', stroke);
        }

        if (color) {
            $('#' + domId + ' .structurizrName').css('fill', color);
            $('#' + domId + ' .structurizrDescription').css('fill', color);
            $('#' + domId + ' .structurizrMetaData').css('fill', color);
            $('#' + domId + ' .structurizrNavigation').css('fill', color);
        }
    }

    function changeColourOfLine(link, color) {
        link.attr('line/stroke', color);
        link.label(0, {
            attrs: {
                text: { fill: color }
            }
        });
        link.label(1, {
            attrs: {
                text: { fill: color }
            }
        });
    }

    function hideElement(elementId, opacity) {
        var element = mapOfIdToBox[elementId];
        if (element) {
            var elementView = paper.findViewByModel(element);
            $('#' + elementView.el.id + ' .structurizrElement').css('opacity', opacity);
        }
    }

    function hideBoundaries(opacity) {
        Object.keys(boundariesByElementId).forEach(function(elementId) {
            const cell = boundariesByElementId[elementId];
            var cellView = paper.findViewByModel(cell);
            $('#' + cellView.id).css('opacity', opacity);
        });
    }

    function showBoundaries() {
        Object.keys(boundariesByElementId).forEach(function(elementId) {
            const cell = boundariesByElementId[elementId];
            var cellView = paper.findViewByModel(cell);
            $('#' + cellView.id).css('opacity', 1.0);
        });
    }

    this.stopAnimation = function() {
        animationStarted = false;
        fireAnimationStoppedEvent();

        hideAllLines('1.0');
        hideAllElements('1.0');
        unfadeAllElements();
        runFilter();

        if (this.currentViewIsDynamic()) {
            linesToAnimate = undefined;
        } else if (this.currentViewHasAnimation()) {
            animationSteps = undefined;
        }

        if (this.currentViewIsDynamic()) {
            const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
            const animate = getViewOrViewSetProperty(viewOrFilter, 'structurizr.zoomOnAnimation', 'false') === 'true';
            if (animate) {
                this.zoomToWidthOrHeight();
            }
        }
    };

    function highlightLinesWithOrder(order) {
        const viewOrFilter = (currentFilter !== undefined ? currentFilter : currentView);
        const zoom = getViewOrViewSetProperty(viewOrFilter, 'structurizr.zoomOnAnimation', 'false') === 'true';
        const elementsInStep = [];

        var line = linesToAnimate[animationIndex];
        while (animationIndex < linesToAnimate.length && line.relationshipInView.order === order) {
            showRelationship(line.relationshipInView.id, line.relationshipInView.order);

            var relationship = structurizr.workspace.findRelationshipById(line.relationshipInView.id);

            unfadeElement(relationship.sourceId);
            unfadeElement(relationship.destinationId);

            elementsInStep.push(relationship.sourceId);
            elementsInStep.push(relationship.destinationId);

            animationIndex++;
            line = linesToAnimate[animationIndex];
        }

        if (zoom) {
            zoomFitElements(elementsInStep);
        }
    }

    this.hasElementsSelected = function() {
        return selectedElements.length > 0;
    };

    this.hasElementHighlighted = function() {
        return highlightedElement !== undefined;
    };

    this.getHighlightedElementId = function() {
        return highlightedElement.model.elementInView.id;
    };

    this.hasLinkHighlighted = function() {
        return highlightedLink !== undefined;
    };

    this.getHighlightedRelationshipId = function() {
        return highlightedLink.model.relationshipInView.id;
    };

    this.moveLabelOfHighlightedLink = function(delta) {
        var labels = highlightedLink.model.get('labels');
        if (labels) {
            if (labels[0]) {
                var currentDistance = labels[0].position.distance;
                var newDistance = Math.min(1, Math.max(0, currentDistance + delta));

                for (i = 0; i < labels.length; i++) {
                    var position = labels[i].position;
                    highlightedLink.model.label(i, {
                        position: {
                            distance: newDistance,
                            offset: {
                                x: position.offset.x,
                                y: position.offset.y
                            }
                        }
                    });
                }

                highlightedLink.model.relationshipInView.position = Math.round(newDistance * 100);
                fireWorkspaceChangedEvent();
            }
        }
    };

    this.toggleRoutingOfHighlightedLink = function() {
        if (highlightedLink.model.relationshipInView.routing === undefined) {
            highlightedLink.model.relationshipInView.routing = 'Direct';
            setRouting(highlightedLink.model, 'Direct');

        } else if (highlightedLink.model.relationshipInView.routing === 'Direct') {
            highlightedLink.model.relationshipInView.routing = 'Curved';
            setRouting(highlightedLink.model, 'Curved');

        } else if (highlightedLink.model.relationshipInView.routing === 'Curved') {
            highlightedLink.model.set('vertices', []);
            highlightedLink.model.relationshipInView.routing = 'Orthogonal';
            setRouting(highlightedLink.model, 'Orthogonal');

        } else if (highlightedLink.model.relationshipInView.routing === 'Orthogonal') {
            var relationship = structurizr.workspace.findRelationshipById(highlightedLink.model.relationshipInView.id);
            if (relationship) {
                const configuration = structurizr.ui.findRelationshipStyle(relationship, darkMode);
                highlightedLink.model.relationshipInView.routing = undefined;
                setRouting(highlightedLink.model, configuration.routing);
            }
        }

        fireWorkspaceChangedEvent();
    };

    this.toggleJumpOfHighlightedLink = function() {
        var relationship = structurizr.workspace.findRelationshipById(highlightedLink.model.relationshipInView.id);
        if (relationship) {
            const configuration = structurizr.ui.findRelationshipStyle(relationship, darkMode);

            if (highlightedLink.model.relationshipInView.jump === undefined) {
                highlightedLink.model.relationshipInView.jump = true;
                setJump(highlightedLink.model, true, configuration.thickness);

            } else if (highlightedLink.model.relationshipInView.jump === true) {
                highlightedLink.model.relationshipInView.jump = false;
                setJump(highlightedLink.model, false, configuration.thickness);

            } else if (highlightedLink.model.relationshipInView.jump === false) {
                highlightedLink.model.relationshipInView.jump = undefined;
                setJump(highlightedLink.model, configuration.jump, configuration.thickness);
            }

            fireWorkspaceChangedEvent();
        }
    }

    this.addVertex = function() {
        if (editable) {
            highlightedLink.addVertex({x: currentX, y: currentY});
        }
    };

    this.moveSelectedElementsLeft = function() {
        addToUndoBuffer(getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements())));
        selectedElements.forEach(function(cellView) {
            moveElement(cellView.model, -gridSize, 0);
        });
        moveLinksBetweenSelectedElements(-gridSize, 0);
    };

    this.moveSelectedElementsRight = function() {
        addToUndoBuffer(getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements())));
        selectedElements.forEach(function(cellView) {
            moveElement(cellView.model, gridSize, 0);
        });
        moveLinksBetweenSelectedElements(gridSize, 0);
    };

    this.moveSelectedElementsUp = function() {
        addToUndoBuffer(getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements())));
        selectedElements.forEach(function(cellView) {
            moveElement(cellView.model, 0, -gridSize);
        });
        moveLinksBetweenSelectedElements(0, -gridSize);
    };

    this.moveSelectedElementsDown = function() {
        addToUndoBuffer(getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements())));
        selectedElements.forEach(function(cellView) {
            moveElement(cellView.model, 0, gridSize);
        });
        moveLinksBetweenSelectedElements(0, gridSize);
    };

    function moveElement(element, dx, dy) {
        var x = element.get('position').x;
        var y = element.get('position').y;

        positionElement(element, x+dx, y+dy);
    }

    function positionElement(element, x, y) {
        element.set(
            {
                position: {
                    x: Math.floor(x),
                    y: Math.floor(y)
                }
            }
        );
    }

    this.alignSelectedElementsLeft = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var left = firstSelectedElement.get('position').x;

            selectedElements.forEach(function(cellView) {
                var y = cellView.model.get('position').y;

                positionElement(cellView.model, left, y);
            });
        }
    };

    this.alignSelectedElementsRight = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var right = firstSelectedElement.get('position').x + firstSelectedElement.get('size').width;

            selectedElements.forEach(function(cellView) {
                var y = cellView.model.get('position').y;
                var width = cellView.model.get('size').width;

                positionElement(cellView.model, right - width, y);
            });
        }
    };

    this.alignSelectedElementsVerticalCentre = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var centre = firstSelectedElement.get('position').x + (firstSelectedElement.get('size').width / 2);

            selectedElements.forEach(function(cellView) {
                var y = cellView.model.get('position').y;
                var width = cellView.model.get('size').width;

                positionElement(cellView.model, centre - (width/2), y);
            });
        }
    };

    this.alignSelectedElementsTop = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var top = firstSelectedElement.get('position').y;

            selectedElements.forEach(function(cellView) {
                var x = cellView.model.get('position').x;

                positionElement(cellView.model, x, top);
            });
        }
    };

    this.alignSelectedElementsBottom = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var bottom = firstSelectedElement.get('position').y + firstSelectedElement.get('size').height;

            selectedElements.forEach(function(cellView) {
                var x = cellView.model.get('position').x;
                var height = cellView.model.get('size').height;

                positionElement(cellView.model, x, bottom - height );
            });
        }
    };

    this.alignSelectedElementsHorizontalCentre = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            var firstSelectedElement = selectedElements[0].model;
            var centre = firstSelectedElement.get('position').y + (firstSelectedElement.get('size').height/2);

            selectedElements.forEach(function(cellView) {
                var x = cellView.model.get('position').x;
                var height = cellView.model.get('size').height;

                positionElement(cellView.model, x, centre - (height/2) );
            });
        }
    };

    this.distributeSelectedElementsHorizontally = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            // order the elements from left to right
            selectedElements.sort(function(a,b) {
                var aX = a.model.get('position').x;
                var bX = b.model.get('position').x;

                return aX - bX;
            });

            var firstSelectedElement = selectedElements[0];
            var lastSelectedElement = selectedElements[selectedElements.length-1];

            var totalWidth = (lastSelectedElement.model.get('position').x + lastSelectedElement.model.get('size').width) - firstSelectedElement.model.get('position').x;
            var totalWidthOfElements = 0;
            selectedElements.forEach(function(cellView) {
                totalWidthOfElements += cellView.model.get('size').width;
            });
            var spacingBetweenElements = Math.floor((totalWidth - totalWidthOfElements) / (selectedElements.length-1));

            var x = firstSelectedElement.model.get('position').x;
            for (var i = 1; i < selectedElements.length-1; i++) {
                var currentCellView = selectedElements[i];
                var previousCellView = selectedElements[i-1];

                x += previousCellView.model.get('size').width + spacingBetweenElements;
                var y = currentCellView.model.get('position').y;

                positionElement(currentCellView.model, x, y)
            }
        }
    };

    this.distributeSelectedElementsVertically = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements));

            // order the elements from top to bottom
            selectedElements.sort(function(a,b) {
                var aY = a.model.get('position').y;
                var bY = b.model.get('position').y;

                return aY - bY;
            });

            var firstSelectedElement = selectedElements[0];
            var lastSelectedElement = selectedElements[selectedElements.length-1];

            var totalHeight = (lastSelectedElement.model.get('position').y + lastSelectedElement.model.get('size').height) - firstSelectedElement.model.get('position').y;
            var totalHeightOfElements = 0;
            selectedElements.forEach(function(cellView) {
                totalHeightOfElements += cellView.model.get('size').height;
            });
            var spacingBetweenElements = Math.floor((totalHeight - totalHeightOfElements) / (selectedElements.length-1));

            var y = firstSelectedElement.model.get('position').y;
            for (var i = 1; i < selectedElements.length-1; i++) {
                var currentCellView = selectedElements[i];
                var previousCellView = selectedElements[i-1];

                var x = currentCellView.model.get('position').x;
                y += previousCellView.model.get('size').height + spacingBetweenElements;

                positionElement(currentCellView.model, x, y)
            }
        }
    };

    this.centreDiagramOrSelectedElements = function() {
        if (selectedElements.length > 0) {
            this.centreSelectedElements();
        } else {
            centreDiagram();
        }
    };

    this.centreSelectedElements = function() {
        if (this.hasElementsSelected()) {
            addToUndoBuffer(getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements())));

            return this.centreElements(selectedElements);
        }
    };

    this.centreElements = function(elements) {
        var minX = diagramWidth, minY = diagramHeight;
        var maxX = 0, maxY = 0;
        elements.forEach(function(cellView) {
            var x = cellView.model.get('position').x;
            var y = cellView.model.get('position').y;
            var width = cellView.model.get('size').width;
            var height = cellView.model.get('size').height;

            minX = Math.min(x, minX);
            maxX = Math.max(x+width, maxX);

            minY = Math.min(y, minY);
            maxY = Math.max(y+height, maxY);

            var ancestors = cellView.model.getAncestors();
            if (ancestors !== undefined && elements.length > 1) {
                ancestors.forEach(function(ancestor) {
                    var x = ancestor.get('position').x;
                    var y = ancestor.get('position').y;
                    var width = ancestor.get('size').width;
                    var height = ancestor.get('size').height;

                    minX = Math.min(x, minX);
                    maxX = Math.max(x+width, maxX);

                    minY = Math.min(y, minY);
                    maxY = Math.max(y+height, maxY);
                });
            }
        });

        var dx = Math.floor(((diagramWidth - maxX) - minX)/2);
        var dy = Math.floor(((diagramHeight - diagramMetadataHeight - maxY) - minY)/2);

        elements.forEach(function(cellView) {
            moveElement(cellView.model, dx, dy);
        });
        moveLinksBetweenElements(elements, dx, dy);
    };

    function repositionLasso() {
        if (lasso && lassoStart !== undefined && lassoEnd !== undefined) {
            lasso.css({
                left: Math.min(lassoStart.clientX, lassoEnd.clientX),
                top: Math.min(lassoStart.clientY, lassoEnd.clientY),
                width: Math.abs(lassoStart.clientX - lassoEnd.clientX),
                height: Math.abs(lassoStart.clientY - lassoEnd.clientY)
            });
        }
    }

    function lassoElements(append) {
        if (append === false) {
            self.deselectAllElements();
        }

        var lassoBoundingBox = {
            left: Math.min(lassoStart.x, lassoEnd.x),
            top: Math.min(lassoStart.y, lassoEnd.y),
            right: Math.max(lassoStart.x, lassoEnd.x),
            bottom: Math.max(lassoStart.y, lassoEnd.y)
        };

        graph.getElements().forEach(function(cell) {
            if (cell.elementInView && cell.positionCalculated === false) {
                var elementBoundingBox = cell.getBBox();
                elementBoundingBox.left = elementBoundingBox.x;
                elementBoundingBox.top = elementBoundingBox.y;
                elementBoundingBox.right = elementBoundingBox.left + elementBoundingBox.width;
                elementBoundingBox.bottom = elementBoundingBox.top + elementBoundingBox.height;

                if (
                    elementBoundingBox.right < lassoBoundingBox.left ||
                    lassoBoundingBox.right < elementBoundingBox.left ||
                    elementBoundingBox.bottom < lassoBoundingBox.top ||
                    lassoBoundingBox.bottom < elementBoundingBox.top) {
                    // do nothing
                } else {
                    selectElement(paper.findViewByModel(cell));
                }
            }
        });

        lassoStart = undefined;
        lassoEnd = undefined;
    }

    function moveLinksBetweenSelectedElements(dx, dy) {
        return moveLinksBetweenElements(selectedElements, dx, dy);
    }

    function moveLinksBetweenElements(elements, dx, dy) {
        if (dx === 0 && dy === 0) {
            return;
        }

        // find all of the links between selected elements that have vertices defined
        var linksBetweenElements = getLinksBetweenElements(elements);
        linksBetweenElements.forEach(function(link) {
            var oldVertices = link.get('vertices');
            if (oldVertices) {
                var newVertices = [];

                oldVertices.forEach(function(oldVertex) {
                    var newVertex = { x: oldVertex.x + dx, y: oldVertex.y + dy };
                    newVertices.push(newVertex);
                });

                link.set('vertices', newVertices);
            }
        })
    }

    function getLinksBetweenSelectedElements() {
        return getLinksBetweenElements(selectedElements);
    }

    function getLinksBetweenElements(elements) {
        var linksBetweenElements = [];

        var links = graph.getLinks();
        if (links) {
            links.forEach(function(link) {
                var sourceCellView = paper.findViewByModel(graph.getCell(link.get('source')));
                var targetCellView = paper.findViewByModel(graph.getCell(link.get('target')));

                if (elements.indexOf(sourceCellView) > -1 && elements.indexOf(targetCellView) > -1) {
                    linksBetweenElements.push(link);
                }
            })
        }

        return linksBetweenElements;
    }

    function addPaperEventHandlers() {
        paper.on('cell:mouseover', function (cellView, evt) {
            if (cellView.model.elementInView) {
                highlightedElement = cellView;

                // and highlight connections to/from element
                const connectionsForElement = connections[cellView.model.elementInView.id];
                if (connectionsForElement) {
                    connectionsForElement.forEach(function (cellView) {
                        highlightRelationship(cellView);
                    });
                }
            }

            if (cellView.model.relationshipInView) {
                highlightedLink = cellView;

                var point = V(paper.viewport).toLocalPoint(evt.clientX, evt.clientY);
                currentX = point.x;
                currentY = point.y;

                highlightRelationship(cellView);
            }

            const x = evt.clientX;
            const y = evt.clientY;

            if (evt.altKey && tooltip.isVisible()) {
                // do nothing ... sticky tooltip mode
            } else {
                if (tooltip && tooltip.isEnabled()) {
                    if (cellView.model.elementInView) {
                        showTooltipForElement(cellView, x, y);
                    } else if (cellView.model.relationshipInView) {
                        showTooltipForRelationship(cellView, x, y);
                    }
                }
            }
        });

        paper.on('cell:mouseout', function (cell, evt) {
            if (evt.altKey && tooltip && tooltip.isVisible()) {
                // do nothing ... sticky tooltip mode
            } else {
                if (tooltip) {
                    tooltip.hide();
                }

                if (highlightedElement) {
                    // and unhighlight connections to/from element
                    if (cell.model.elementInView) {
                        const connectionsForElement = connections[cell.model.elementInView.id];
                        if (connectionsForElement) {
                            connectionsForElement.forEach(function (cellView) {
                                unhighlightRelationship(cellView);
                            });
                        }
                        highlightedElement = undefined;
                    }
                }

                if (highlightedLink) {
                    unhighlightRelationship(cell);
                    highlightedLink = undefined;
                }
            }
        });

        paper.on('cell:pointerdown', function (cell, evt, x, y) {
            window.focus();

            if (!cell.getConnectionLength) {
                // an element has been clicked
                if (self.hasElementsSelected()) {
                    if (selectedElements.indexOf(cell) === -1) {
                        // an unselected element has been clicked
                        previousPositions = getCurrentElementPositions([cell]);
                    } else {
                        previousPositions = getCurrentElementPositions(selectedElements).concat(getCurrentLinkPositions(getLinksBetweenSelectedElements()));
                    }
                } else {
                    // a single element has been clicked
                    previousPositions = getCurrentElementPositions([cell]);
                }
            } else {
                // a link has been clicked
                if (editable) {
                    previousPositions = getCurrentLinkPositions([cell.model]);
                }
            }
        });

        paper.on('cell:pointerup', function (cell, evt, x, y) {
            if (previousPositions && previousPositions.length > 0) {
                if (!cell.getConnectionLength) {
                    previousPositions.forEach(function (previousPosition) {
                        if (previousPosition.element === cell.model) {
                            if ((previousPosition.x !== cell.model.position().x) || (previousPosition.y !== cell.model.position().y)) {
                                addToUndoBuffer(previousPositions);
                            }
                        }
                    });
                } else {
                    addToUndoBuffer(previousPositions);
                }
            }
        });

        paper.on('blank:pointerdown', function (evt, x, y) {
            window.focus();

            if (evt.altKey === true) {
                enableCanvasDragging();
            } else if (self.isEditable()) {
                lassoStart = {
                    x: x,
                    y: y,
                    clientX: evt.clientX,
                    clientY: evt.clientY
                };

                lassoEnd = {
                    x: x,
                    y: y,
                    clientX: evt.clientX,
                    clientY: evt.clientY
                };

                repositionLasso();
                if (lasso) {
                    lasso.removeClass('hidden');
                }

                canvas.mousemove(lassoMouseMove);
                if (lasso) {
                    lasso.mousemove(lassoMouseMove);
                }
            } else {
                parentElement.focus();
            }
        });

        paper.on('blank:pointerup', function (evt, x, y) {
            if (evt.altKey === true) {
                if (self.isEditable()) {
                    disableCanvasDragging();
                }
            } else if (self.isEditable()) {
                if (lasso) {
                    lasso.addClass('hidden');
                    lassoEnd = {
                        x: x,
                        y: y
                    };

                    if (lassoStart === undefined || (lassoStart.x === lassoEnd.x && lassoStart.y === lassoEnd.y)) {
                        lassoStart = undefined;
                        lassoEnd = undefined;

                        self.deselectAllElements();
                    } else {
                        canvas.unbind('mousemove', lassoMouseMove);
                        lasso.unbind('mousemove', lassoMouseMove);
                        lassoElements(evt.shiftKey || evt.altKey);
                    }
                }
            }
        });

        paper.on('cell:pointerclick', function (cellView, evt, x, y) {
            if (self.isEditable()) {
                if (cellView.model.elementInView && cellView.model.positionCalculated === false) {
                    var cellViewIsSelected = cellView.selected;
                    var multipleCellsAreSelected = selectedElements.length > 1;

                    if (!evt.shiftKey && !evt.altKey) {
                        self.deselectAllElements();

                        if (multipleCellsAreSelected) {
                            cellView.selected = true;
                        }
                    }

                    if (cellViewIsSelected === undefined) {
                        cellView.selected = true;
                    } else {
                        cellView.selected = !cellViewIsSelected;
                    }

                    if (cellView.selected === true) {
                        selectElement(cellView);
                    } else {
                        deselectElement(cellView);
                    }
                } else {
                    self.deselectAllElements();
                }
            }
        });
    }

    const lassoMouseMove = function(event) {
        if (lassoEnd !== undefined) {
            lassoEnd.clientX = event.pageX;
            lassoEnd.clientY = event.pageY;
            repositionLasso();
        }
    };

    function enableCanvasDragging() {
        viewport.css('cursor', 'grabbing');
        viewport.bind("mousedown", dragMouseDown);
        viewport.bind('mouseup', dragStop);
        viewport.bind("mouseleave", dragStop);
    }

    function disableCanvasDragging() {
        viewport.css('cursor', '');
        viewport.unbind("mousedown", dragMouseDown);
        viewport.unbind('mouseup', dragStop);
        viewport.unbind("mouseleave", dragStop);
        dragStop();
    }

    function dragMouseDown(event) {
        dragStartPosition = {
            x: event.clientX,
            y: event.clientY
        };
        scrollStartPosition = {
            x: viewport.scrollLeft(),
            y: viewport.scrollTop()
        };

        viewport.bind("mousemove", dragMouseMove);
    }

    function dragMouseMove(event) {
        if (event !== undefined) {
            var x = event.clientX;
            var y = event.clientY;

            var deltaX = (dragStartPosition.x - x);
            var deltaY = (dragStartPosition.y - y);
            viewport.scrollLeft(scrollStartPosition.x + (deltaX * scale));
            viewport.scrollTop(scrollStartPosition.y + (deltaY * scale));
        }
    }

    function dragStop(event) {
        dragStartPosition = undefined;
        viewport.unbind("mousemove", dragMouseMove);
    }

    this.setKeyboardShortcutsEnabled = function(bool) {
        keyboardShortcutsEnabled = bool;
    };

    this.areKeyboardShortcutsEnabled = function() {
        return keyboardShortcutsEnabled;
    };

    function getCurrentElementPositions(elementViews) {
        var previousPositions = [];
        elementViews.forEach(function(elementView) {
            var allElements = elementView.model.getEmbeddedCells({ deep: true });
            allElements.push(elementView.model);

            allElements.forEach(function(element) {
                previousPositions.push({
                    type: 'element',
                    element: element,
                    x: element.get('position').x,
                    y: element.get('position').y
                });
            });
        });

        return previousPositions;
    }

    function getCurrentLinkPositions(links) {
        var previousPositions = [];
        links.forEach(function(link) {
            previousPositions.push({
                type: 'link',
                link: link,
                vertices: link.get('vertices')
            });
        });

        return previousPositions;
    }

    function addToUndoBuffer(previousPositions) {
        undoStack.push(previousPositions);
        fireWorkspaceChangedEvent();
    }

    this.undo = function() {
        if (!undoStack.isEmpty()) {
            var previousPositions = undoStack.pop();
            previousPositions.forEach(function(position) {
                if (position.type === 'element') {
                    positionElement(position.element, position.x, position.y);
                } else {
                    position.link.set('vertices' , position.vertices);
                }

                fireWorkspaceChangedEvent();
            });
        }
    };

    this.removeAllVertices = function() {
        addToUndoBuffer(getCurrentLinkPositions(graph.getLinks()));

        graph.getLinks().forEach(function (link) {
            link.set('vertices', []);
        });
    };

    this.applyAutomaticLayout = function(rankDirection, rankSeparation, nodeSeparation, edgeSeparation, linkVertices) {
        try {
            var cellViews = [];
            cells.forEach(function(cell) {
                var element = paper.findViewByModel(cell);
                if (element.model.positionCalculated === false) {
                    cellViews.push(element);
                }
            });
            addToUndoBuffer(getCurrentElementPositions(cellViews).concat(getCurrentLinkPositions(graph.getLinks())));

            this.removeAllVertices();

            var direction;
            switch(rankDirection) {
                case 'TopBottom':
                    direction = 'TB';
                    break;
                case 'BottomTop':
                    direction = 'BT';
                    break;
                case 'LeftRight':
                    direction = 'LR';
                    break;
                case 'RightLeft':
                    direction = 'RL';
                    break;
                default:
                    direction = 'TB';
            }

            joint.layout.DirectedGraph.layout(graph, {
                rankSep: rankSeparation,
                nodeSep: nodeSeparation,
                edgeSep: edgeSeparation,
                setLinkVertices: linkVertices,
                setLabels: true,
                rankDir: direction,
                marginX: 0,
                marginY: 0,
                // ranker: 'network-simplex',
                // ranker: 'tight-tree',
                // ranker: 'longest-path',
                resizeClusters: true,
                clusterPadding: {
                    top: clusterPadding,
                    left: clusterPadding,
                    right: clusterPadding,
                    bottom: clusterPadding
                }
            });

            lines.forEach(function(line) {
                const labels = line.get('labels');
                if (labels !== undefined && labels.length > 1) {
                    labels[0].position.offset = labels[0].position.calculatedOffset;
                    labels[1].position.distance = labels[0].position.distance;
                }
            });

            this.autoPageSize();
            this.zoomToWidthOrHeight();

            centreDiagram();
            diagramRendered = true;
        } catch (err) {
            console.error('There was an error applying the automatic layout: ' + err);
        }
    }

    function centreDiagram() {
        var cellViews = [];
        cells.forEach(function (cell) {
            var element = paper.findViewByModel(cell);
            if (element.model.positionCalculated === false) {
                cellViews.push(element);
            }
        });

        var contentArea = findContentArea(false, 0);

        var dx = Math.floor(((diagramWidth - contentArea.maxX) - contentArea.minX)/2);
        var dy = Math.floor(((diagramHeight - diagramMetadataHeight - contentArea.maxY) - contentArea.minY)/2);

        cellViews.forEach(function(cellView) {
            moveElement(cellView.model, dx, dy);
        });
        moveLinksBetweenElements(cellViews, dx, dy);
    }

    this.undoStackIsEmpty = function() {
        return undoStack.isEmpty();
    };

    function showTooltipForElement(cellView, x, y) {
        const element = structurizr.workspace.findElementById(cellView.model.elementInView.id);
        var style = cellView.model._computedStyle;

        if (filter.perspective !== undefined && elementHasPerspective(element) === false) {
            return;
        }

        if (tooltip === undefined) {
            return;
        }

        if (filter.perspective) {
            style = cellView.model._perspectiveStyle;
        }

        tooltip.showTooltipForElement(element, style, x, y, false, filter.perspective);
    }

    function showTooltipForRelationship(cellView, x, y) {
        const relationship = structurizr.workspace.findRelationshipById(cellView.model.relationshipInView.id);
        const relationshipInView = cellView.model.relationshipInView;
        var style = cellView.model._computedStyle;

        if (filter.perspective !== undefined && relationshipHasPerspective(relationship) === false) {
            return;
        }

        if (tooltip === undefined) {
            return;
        }

        if (filter.perspective) {
            style = cellView.model._perspectiveStyle;
        }


        tooltip.showTooltipForRelationship(relationship, relationshipInView, style, x, y, false, filter.perspective);
    }

    this.toggleMetadata = function() {
        metadataEnabled = !metadataEnabled;

        if (metadataEnabled) {
            showMetadata();
        } else {
            hideMetadata();
        }
    };

    function showMetadata() {
        $('.structurizrElement .structurizrMetaData').css('display', 'block');
        $('.joint-link .structurizrMetaData').css('display', 'block')
    }

    function hideMetadata() {
        $('.structurizrElement .structurizrMetaData').css('display', 'none');
        $('.joint-link .structurizrMetaData').css('display', 'none')
    }

    this.toggleDescription = function() {
        descriptionEnabled = !descriptionEnabled;

        if (descriptionEnabled) {
            showDescription();
        } else {
            hideDescription();
        }
    };

    function showDescription() {
        $('.structurizrElement .structurizrDescription').css('display', 'block');
        $('.joint-link .structurizrDescription').css('display', 'block');
    }

    function hideDescription() {
        $('.structurizrElement .structurizrDescription').css('display', 'none');
        $('.joint-link .structurizrDescription').css('display', 'none');
    }

    this.showDiagramScope = function(bool) {
        this.stopAnimation();

        if (bool) {
            hideAllElements(1.0);
            fadeAllElements();
            hideAllLines(0.2);

            if (currentView.type === 'SystemContext') {
                unfadeElement(currentView.softwareSystemId);
            } else if (currentView.type === 'Container') {
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element && element.parentId === currentView.softwareSystemId) {
                        unfadeElement(element.id);
                    }
                })
            } else if (currentView.type === 'Component') {
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element && element.parentId === currentView.containerId) {
                        unfadeElement(element.id);
                    }
                })
            } else if (currentView.type === 'Dynamic' && currentView.elementId !== undefined) {
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element && element.parentId === currentView.elementId) {
                        unfadeElement(element.id);
                    }
                })
            } else if (currentView.type === 'Deployment' && currentView.softwareSystemId !== undefined) {
                currentView.elements.forEach(function(elementInView) {
                    var element = structurizr.workspace.findElementById(elementInView.id);
                    if (element.type === 'ContainerInstance') {
                        var container = structurizr.workspace.findElementById(element.containerId);
                        if (container && container.parentId === currentView.softwareSystemId) {
                            unfadeElement(element.id);
                        }
                    }
                })
            }
        } else {
            hideAllElements(1.0);
            unfadeAllElements();
            hideAllLines(1.0);
        }
    }

    this.onkeydown = function(callback) {
        onKeyDownEventHandler = callback;
    }

    this.onkeypress = function(callback) {
        onKeyPressEventHandler = callback;
    }

    $(document).keydown(function(e) {
        if (self.areKeyboardShortcutsEnabled()) {
            const leftArrow = 37;
            const pageUp = 33;
            const rightArrow = 39;
            const pageDown = 34;
            const upArrow = 38;
            const downArrow = 40;

            if ((e.which === leftArrow || e.which === pageUp)) {
                // move selected elements left or move to previous diagram
                if (self.hasElementsSelected() && self.isEditable()) {
                    self.moveSelectedElementsLeft();
                    e.preventDefault();
                    return;
                } else if (Math.round(canvas.innerWidth()) > Math.round(viewport.innerWidth())) {
                    viewport.scrollLeft(viewport.scrollLeft() - (viewport.innerWidth() / 10));
                    e.preventDefault();
                    return;
                }
            } else if ((e.which === rightArrow || e.which === pageDown)) {
                // move selected elements right or move to next diagram
                if (self.hasElementsSelected() && self.isEditable()) {
                    self.moveSelectedElementsRight();
                    e.preventDefault();
                    return;
                } else if (Math.round(canvas.innerWidth()) > Math.round(viewport.innerWidth())) {
                    viewport.scrollLeft(viewport.scrollLeft() + (viewport.innerWidth() / 10));
                    e.preventDefault();
                    return;
                }
            } else if ((e.which === upArrow)) {
                // move selected elements up or move to the previous diagram
                if (self.hasLinkHighlighted() && self.isEditable()) {
                    self.moveLabelOfHighlightedLink(0.05);
                    e.preventDefault();
                    return;
                } else if (self.hasElementsSelected() && self.isEditable()) {
                    self.moveSelectedElementsUp();
                    e.preventDefault();
                    return;
                } else if (Math.round(canvas.innerHeight()) > Math.round(viewport.innerHeight())) {
                    viewport.scrollTop(viewport.scrollTop() - (viewport.innerHeight() / 10));
                    e.preventDefault();
                    return;
                }
            } else if ((e.which === downArrow)) {
                // move selected elements down or move to the next diagram
                if (self.hasLinkHighlighted() && self.isEditable()) {
                    self.moveLabelOfHighlightedLink(-0.05);
                    e.preventDefault();
                    return;
                } else if (self.hasElementsSelected() && self.isEditable()) {
                    self.moveSelectedElementsDown();
                    e.preventDefault();
                    return;
                } else if (Math.round(canvas.innerHeight()) > Math.round(viewport.innerHeight())) {
                    viewport.scrollTop(viewport.scrollTop() + (viewport.innerHeight() / 10));
                    e.preventDefault();
                    return;
                }
            }

            // if we got this far, now run the provided handler
            if (onKeyDownEventHandler) {
                onKeyDownEventHandler(e);
            }
        }
    });

    $(document).keypress(function(e) {
        if (self.areKeyboardShortcutsEnabled()) {
            var plus = 43;
            var equals = 61;
            var minus = 45;
            var comma = 44;
            var dot = 46;
            var a = 97;
            var c = 99;
            var d = 100;
            var f = 102;
            var h = 104;
            var m = 109;
            var n = 110;
            var r = 114;
            var u = 117;
            var v = 118;
            var w = 119;

            // if we got this far, now run the provided handler
            if (onKeyPressEventHandler) {
                onKeyPressEventHandler(e);
            }
        }
    });

};

structurizr.shapes = {};

structurizr.shapes.Box = joint.dia.Element.define(
    'structurizr.box',
    {
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrBox': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'rect',
            },
            '.structurizrIcon': {
                ref: 'rect'
            }
        }
    },
    {
        markup: '<g class="structurizrElement"><rect class="structurizrBox structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>'
    }
);

structurizr.shapes.Boundary = joint.dia.Element.define(
    'structurizr.boundary',
    {
        attrs: {
            rect: {
                width: 0,
                height: 0,
                rx: 0,
                ry: 0
            },
            '.structurizrBoundary': {
                'stroke-width': '2',
                'pointer-events': 'none'
            },
            '.structurizrName': {
                'font-weight': 'normal',
                'font-size': '21px',
                ref: 'rect',
                'text-anchor': 'start',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                'font-weight': 'normal',
                'font-size': '15px',
                ref: 'rect',
                'text-anchor': 'start',
                'pointer-events': 'visible'
            },
            '.structurizrIcon': {
                'pointer-events': 'visible'
            }
        }
    },
    {
        markup: '<g><rect class="structurizrBoundary structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><image class="structurizrIcon" /></g>'
    }
);

structurizr.shapes.DeploymentNode = joint.dia.Element.define(
    'structurizr.deploymentNode',
    {
        attrs: {
            rect: {
                width: 100,
                height: 100,
                rx: 10,
                ry: 10
            },
            '.structurizrDeploymentNode': {
                'stroke-width': '1',
                'pointer-events': 'none'
            },
            '.structurizrName': {
                'font-weight': 'normal',
                'font-size': '20px',
                ref: 'rect',
                'text-anchor': 'start',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                'font-weight': 'normal',
                'font-size': '15px',
                ref: 'rect',
                'text-anchor': 'start',
                'pointer-events': 'visible'
            },
            '.structurizrInstanceCount': {
                'font-size': '40px',
                'font-weight': 'normal',
                'fill': '#555555',
                ref: 'rect',
                'ref-x': 0.98,
                'text-anchor': 'end',
                'pointer-events': 'visible'
            },
            '.structurizrIcon': {
                'pointer-events': 'visible'
            }
        }
    },
    {
        markup: '<g class="structurizrElement"><rect class="structurizrDeploymentNode structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrInstanceCount"/><image class="structurizrIcon" /></g>'
    }
);

structurizr.shapes.DiagramTitle = joint.dia.Element.extend({
    markup: '<g><text class="structurizrDiagramTitle structurizrMetadata"/></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.diagramTitle',
        attrs: {
            '.structurizrDiagramTitle': {
                'font-weight': 'normal',
                'text-anchor': 'start',
                fill: '#000000',
                'pointer-events': 'none',
                'display': 'none'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.DiagramDescription = joint.dia.Element.extend({
    markup: '<g><text class="structurizrDiagramDescription structurizrMetadata"/></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.diagramDescription',
        attrs: {
            '.structurizrDiagramDescription': {
                'font-weight': 'normal',
                'text-anchor': 'start',
                fill: '#777777',
                'pointer-events': 'none',
                'display': 'none'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.DiagramMetadata = joint.dia.Element.extend({
    markup: '<g><text class="structurizrDiagramMetadata structurizrMetadata"/></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.diagramMetadata',
        attrs: {
            '.structurizrDiagramMetadata': {
                'font-weight': 'normal',
                'text-anchor': 'start',
                fill: '#777777',
                'pointer-events': 'none',
                'display': 'none'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.BrandingImage = joint.dia.Element.extend({
    markup: '<g><image class="structurizrBrandingImage structurizrMetadata"/></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.brandingImage',
        attrs: {
            '.structurizrBrandingImage': {
                'pointer-events': 'none'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.ImageView = joint.dia.Element.extend({
    markup: '<g><image class="structurizrImageView"/></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.image',
        attrs: {
            '.structurizrImage': {
                'pointer-events': 'none'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Relationship = joint.shapes.standard.Link;

structurizr.shapes.Person = joint.dia.Element.extend({
    markup: '<g class="structurizrElement structurizrPerson"><rect class="structurizrPersonBody structurizrHighlightableElement" x="0" y="175" width="450" height="250" rx="70" /><circle class="structurizrPersonHead structurizrHighlightableElement" cx="225" cy="100" r="100" /><line class="structurizrPersonRightArm" x1="90" y1="300" x2="90" y2="450" /><line class="structurizrPersonLeftArm" x1="360" y1="300" x2="360" y2="450" /><text class="structurizrName"/><text class="structurizrMetaData" /><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.person',
        attrs: {
            '.structurizrPersonHead': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrPersonBody': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrPersonBody',
                'ref-x': 0.5,
                'ref-y': 0.25,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrPersonBody',
                'ref-x': 0.5,
                'ref-y': 0.40,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrPersonBody',
                'ref-x': 0.5,
                'ref-y': 0.60,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrPersonBody',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrPersonBody'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Robot = joint.dia.Element.extend({
    markup: '<g class="structurizrElement structurizrRobot"><rect class="structurizrRobotBody structurizrHighlightableElement" x="0" y="175" width="450" height="250" rx="30" /><rect class="structurizrRobotEars structurizrHighlightableElement" rx="10" /><rect class="structurizrRobotHead structurizrHighlightableElement" rx="30" /><line class="structurizrRobotRightArm" x1="90" y1="300" x2="90" y2="450" /><line class="structurizrRobotLeftArm" x1="360" y1="300" x2="360" y2="450" /><text class="structurizrName"/><text class="structurizrMetaData" /><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.robot',
        attrs: {
            '.structurizrRobotHead': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrRobotEars': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrRobotBody': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrRobotBody',
                'ref-x': 0.5,
                'ref-y': 0.25,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrRobotBody',
                'ref-x': 0.5,
                'ref-y': 0.40,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrRobotBody',
                'ref-x': 0.5,
                'ref-y': 0.60,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrRobotBody',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrRobotBody'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Cylinder = joint.dia.Element.extend({
    markup: '<g class="structurizrElement structurizrCylinder"><path class="structurizrCylinderPath structurizrHighlightableElement" d=""></path><rect class="structurizrCylinderFace"></rect><text class="structurizrName"/><text class="structurizrMetaData" /><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.cylinder',
        attrs: {
            '.structurizrCylinderPath': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrCylinderFace': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.25,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.43,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.60,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'rect',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'rect'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Pipe = joint.dia.Element.extend({
    markup: '<g class="structurizrElement structurizrPipe"><path class="structurizrPipePath structurizrHighlightableElement" d=""></path><rect class="structurizrPipeFace"></rect><text class="structurizrName"/><text class="structurizrMetaData" /><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.pipe',
        attrs: {
            '.structurizrPipePath': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrPipeFace': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrPipeFace',
                'ref-x': 0.5,
                'ref-y': 0.25,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.43,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.60,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'rect',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'rect'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Folder = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrFolderTab structurizrHighlightableElement" /><rect class="structurizrFolderBody structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.folder',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrFolderTab': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'

            },
            '.structurizrFolderBody': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'

            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrFolderBody',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrFolderBody',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrFolderBody',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrFolderBody',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrFolderBody'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Component = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrComponent structurizrHighlightableElement"/><rect class="structurizrComponentBlockTop structurizrHighlightableElement" /><rect class="structurizrComponentBlockBottom structurizrHighlightableElement" /><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.component',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrComponentBlockTop': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'

            },
            '.structurizrComponentBlockBottom': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'

            },
            '.structurizrComponent': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'

            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrComponent',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrComponent',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrComponent',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrComponent',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrComponent'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Ellipse = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><ellipse class="structurizrEllipse structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.ellipse',
        attrs: {
            ellipse: {
            },
            '.structurizrEllipse': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'ellipse',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'ellipse',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'ellipse',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'ellipse',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'ellipse'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Hexagon = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><polygon class="structurizrHexagon structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.hexagon',
        attrs: {
            polygon: {
            },
            '.structurizrHexagon': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'polygon',
                'font-weight': 'normal',
                'ref-x': 0.50,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'polygon'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Diamond = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><polygon class="structurizrDiamond structurizrHighlightableElement"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.diamond',
        attrs: {
            polygon: {
            },
            '.structurizrDiamond': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'polygon',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'polygon',
                'font-weight': 'normal',
                'ref-x': 0.50,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'polygon'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.WebBrowser = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrWebBrowser structurizrHighlightableElement"/><rect class="structurizrWebBrowserPanel"/><ellipse class="structurizrWebBrowserButton1"/><ellipse class="structurizrWebBrowserButton2"/><ellipse class="structurizrWebBrowserButton3"/><rect class="structurizrWebBrowserUrlBar"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.webBrowser',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrWebBrowser': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrWebBrowserPanel': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrWebBrowserUrlBar': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrWebBrowserPanel',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrWebBrowserPanel',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrWebBrowserPanel',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrWebBrowserPanel',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrWebBrowserPanel'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Window = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrWindow structurizrHighlightableElement"/><rect class="structurizrWindowPanel"/><ellipse class="structurizrWindowButton1"/><ellipse class="structurizrWindowButton2"/><ellipse class="structurizrWindowButton3"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.window',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrWindow': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrWindowPanel': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrWindowPanel',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrWindowPanel',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrWindowPanel',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrWindowPanel',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrWindowPanel'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Terminal = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrTerminal structurizrHighlightableElement"/><rect class="structurizrTerminalPanel"/><text class="structurizrTerminalPrompt"/><ellipse class="structurizrTerminalButton1"/><ellipse class="structurizrTerminalButton2"/><ellipse class="structurizrTerminalButton3"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.terminal',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrTerminal': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrTerminalPanel': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrTerminalPrompt': {
                'text': '>_',
                'font-family': 'Courier New, Arial',
                'font-weight': 'bold',
                'font-size': 50,
                ref: 'rect',
                'x': 50,
                'y': 90,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: '.structurizrTerminalPanel',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: '.structurizrTerminalPanel',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: '.structurizrTerminalPanel',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: '.structurizrTerminalPanel',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: '.structurizrTerminalPanel'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.Shell = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrShell structurizrHighlightableElement"/><text class="structurizrShellPrompt"/><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.shell',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrShell': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrShellPrompt': {
                'text': '>_',
                'font-family': 'Courier New, Arial',
                'font-weight': 'bold',
                'font-size': 50,
                ref: 'rect',
                'x': 50,
                'y': 50,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'rect',
            },
            '.structurizrIcon': {
                ref: 'rect'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.shapes.MobileDevice = joint.dia.Element.extend({
    markup: '<g class="structurizrElement"><rect class="structurizrMobileDevice structurizrHighlightableElement"/><rect class="structurizrMobileDeviceDisplay"/><ellipse class="structurizrMobileDeviceButton"/><line class="structurizrMobileDeviceSpeaker" style="stroke-width:2px" /><text class="structurizrName"/><text class="structurizrMetaData"/><text class="structurizrDescription"/><g class="structurizrNavigation"><g class="structurizrZoom" /><g class="structurizrDocumentation" /><g class="structurizrDecisions" /><g class="structurizrLink" /></g><image class="structurizrIcon" /></g>',
    defaults: joint.util.deepSupplement({
        type: 'structurizr.mobileDevice',
        attrs: {
            rect: {
                rx: 1,
                ry: 1
            },
            '.structurizrMobileDevice': {
                'stroke-width': 2,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrMobileDeviceDisplay': {
                'stroke-width': 0,
                'pointer-events': 'visiblePainted'
            },
            '.structurizrName': {
                'font-weight': 'bold',
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.15,
                'text-anchor': 'middle',
                'pointer-events': 'visible'
            },
            '.structurizrMetaData': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.30,
                'text-anchor': 'middle'
            },
            '.structurizrDescription': {
                ref: 'rect',
                'ref-x': 0.5,
                'ref-y': 0.45,
                'text-anchor': 'middle'
            },
            '.structurizrNavigation': {
                ref: 'rect',
                'font-weight': 'normal',
                'ref-x': 0.5,
                'text-anchor': 'middle'
            },
            '.structurizrIcon': {
                ref: 'rect'
            }
        }
    }, joint.dia.Element.prototype.defaults)
});

structurizr.ui.PaperSizes = function() {

    const definitions = {};

    definitions['A6_Portrait'] = {
        width: 1240,
        height: 1748
    };

    definitions['A6_Landscape'] = {
        width: 1748,
        height: 1240
    };

    definitions['A5_Portrait'] = {
        width: 1748,
        height: 2480
    };

    definitions['A5_Landscape'] = {
        width: 2480,
        height: 1748
    };

    definitions['A4_Portrait'] = {
        width: 2480,
        height: 3508
    };

    definitions['A4_Landscape'] = {
        width: 3508,
        height: 2480
    };

    definitions['A3_Portrait'] = {
        width: 3508,
        height: 4961
    };

    definitions['A3_Landscape'] = {
        width: 4961,
        height: 3508
    };

    definitions['A2_Portrait'] = {
        width: 4961,
        height: 7016
    };

    definitions['A2_Landscape'] = {
        width: 7016,
        height: 4961
    };

    definitions['A1_Portrait'] = {
        width: 7016,
        height: 9933
    };

    definitions['A1_Landscape'] = {
        width: 9933,
        height: 7016
    };

    definitions['A0_Portrait'] = {
        width: 9933,
        height: 14043
    };

    definitions['A0_Landscape'] = {
        width: 14043,
        height: 9933
    };

    definitions['Letter_Portrait'] = {
        width: 2550,
        height: 3300
    };

    definitions['Letter_Landscape'] = {
        width: 3300,
        height: 2550
    };

    definitions['Legal_Portrait'] = {
        width: 2550,
        height: 4200
    };

    definitions['Legal_Landscape'] = {
        width: 4200,
        height: 2550
    };

    definitions['Slide_4_3'] = {
        width: 3306,
        height: 2480
    };

    definitions['Slide_16_9'] = {
        width: 3508,
        height: 1973
    };

    definitions['Slide_16_10'] = {
        width: 3508,
        height: 2193
    };

    this.getDimensions = function(paperSize) {
        return definitions[paperSize];
    };

};