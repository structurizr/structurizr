<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<script type="text/javascript" src="<c:url value="/static/js/structurizr-embed.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/structurizr-content.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/structurizr-documentation.js" />"></script>
<script type="text/javascript" src="/static/js/markdown-it-13.0.1.min.js"></script>
<script type="text/javascript" src="/static/js/katex-0.16.27.min.js"></script>
<script type="text/javascript" src="/static/js/asciidoctor-2.2.6.min.js"></script>

<link href="/static/css/katex-0.16.27.min.css" rel="stylesheet" media="screen" />
<link href="<c:url value="/static/css/structurizr-asciidoctor.css" />" rel="stylesheet" media="screen" />
<link href="<c:url value="/static/css/structurizr-documentation.css" />" rel="stylesheet" media="screen" />

<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<style id="brandingStyles"></style>

<div class="row" style="padding-top: 20px; padding-bottom: 0; margin-left: 0; margin-right: 0">
    <div class="col-2" style="padding-left: 30px">
        <div id="documentationNavigationPanel" class="hidden-xs">

            <c:if test="${not empty workspace.branch || not empty param.version}">
                <div style="margin-top: 20px">
                    <c:if test="${not empty workspace.branch}">
                        <div style="margin-bottom: 10px;">
                            <span class="label label-branch"><img src="/static/bootstrap-icons/bezier2.svg" class="icon-sm icon-white" /> ${workspace.branch}</span>
                        </div>
                    </c:if>
                    <c:if test="${not empty param.version}">
                        <div style="margin-bottom: 10px;">
                            <span class="label label-version"><img src="/static/bootstrap-icons/clock-history.svg" class="icon-sm icon-white" /> ${workspace.userFriendlyInternalVersion}</span>
                        </div>
                    </c:if>
                </div>
            </c:if>

            <div id="documentationNavigation"></div>

            <div class="navigationItemSeparator"></div>

            <div class="navigationItemSeparator"></div>

            <div class="navigationItemSeparator"></div>

            <div class="navigationItem">
                <a id="exportLink" href="" class="hidden" title="Export to offline HTML page"><img src="/static/bootstrap-icons/filetype-html.svg" class="icon-sm" /></a> |
                <a id="renderingModeLightLink" href="" title="Light"><img src="/static/bootstrap-icons/sun.svg" class="icon-sm" /></a> |
                <a id="renderingModeDarkLink" href="" title="Dark"><img src="/static/bootstrap-icons/moon-fill.svg" class="icon-sm" /></a> |
                <a id="renderingModeSystemLink" href="" title="System"><img src="/static/bootstrap-icons/sliders.svg" class="icon-sm" /></a>
            </div>

            <div id="documentationMetadata">
                <span id="lastModifiedDate"></span>
                <c:if test="${not empty workspace.version}">
                    <br />
                    Version: ${workspace.version}
                </c:if>
            </div>
        </div>
    </div>

    <div class="col-10" style="margin-top: 0; margin-bottom: 0">
        <div id="documentationNavigationDropDownPanel" class="form-group d-sm-none centered" style="margin-top: 0; margin-bottom: 20px">
            <div class="btn-group">
                <select id="documentationNavigationDropDown" class="form-select"></select>
            </div>
        </div>

        <div id="documentationPanel">
            <div id="documentationHeader">
                <div class="centered">
                    <img src="" class="workspaceLogo hidden" alt="Branding logo" />
                </div>
                <h1><span id="documentationScopeName"></span></h1>
            </div>

            <div id="documentationContent"></div>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    const requestedScope = structurizr.util.atob('<c:out value="${scope}" />');

    const documentationNavigation = $('#documentationNavigation');
    const documentationNavigationDropDown = $('#documentationNavigationDropDown');
    const documentationNavigationDropDownPanel = $('#documentationNavigationDropDownPanel');
    var contentRenderer;

    const elementsWithDocumentation = [];
    var sections = [];
    var rendered = false;

    const anchors = [];
    const navigationIdsByAnchor = [];

    var filenameToSection = {};

    const WORKSPACE_SCOPE = '*';

    progressMessage.show('<p>Loading workspace...</p>');

    function workspaceLoaded() {
        init();
    }

    function init() {
        if (structurizr.workspace.hasDocumentation()) {
            contentRenderer = new structurizr.ui.ContentRenderer(
                structurizr.workspace,
                '/static',
                '<c:out value="${urlPrefix}" />',
                ${structurizrConfiguration.safeMode});

            structurizr.scripting = new function() {
                this.exportDocumentationToOfflineHtmlPage = function (callback) {
                    exportDocumentationToOfflineHtmlPage(callback);
                };

                this.isDocumentationRendered = function () {
                    return rendered;
                };
            };

            initDocumentationScopeAndSections();
            show();

            window.onhashchange = function() {
                scrollToHash();
            };

            document.addEventListener("keydown", changeDocumentation);

            $('#documentationPanel').scroll(function() {
                var topPadding = 30;
                var documentationPanelHeight = $('#documentationPanel').innerHeight();
                var documentationPanelOffsetTop = $('#documentationPanel').offset().top - topPadding;
                var done = false;

                $('.headingAnchor').each(function () {
                    if (done === false && $(this).offset().top > documentationPanelOffsetTop && $(this).position().top < documentationPanelHeight) {
                        var hash = '#' + $(this).attr('name');
                        highlightNavigation(undefined, hash);
                        if (window.location.hash !== hash) {
                            window.history.replaceState({}, '', hash);
                        }
                        done = true;
                    }
                });
            });

            $(window).resize(function() {
                resize();
            });

            const timezone = structurizr.workspace.views.configuration.properties['structurizr.timezone'];
            const locale = structurizr.workspace.views.configuration.properties['structurizr.locale'];

            $('#lastModifiedDate').html(new Date(structurizr.workspace.lastModifiedDate).toLocaleString(locale,
                {
                    weekday: 'long',
                    year:'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: 'numeric',
                    minute: 'numeric',
                    timeZone: timezone,
                    timeZoneName : 'long'
                }
            ));
        } else {
            showNoDocumentationPage();
        }

        structurizr.ui.applyWorkspaceLogo();
        resize();
        progressMessage.hide();
    }

    function initDocumentationScopeAndSections() {
        if (structurizr.workspace.documentation.sections.length > 0) {
            elementsWithDocumentation.push(WORKSPACE_SCOPE);

            if (requestedScope === WORKSPACE_SCOPE) {
                sections = structurizr.workspace.documentation.sections;
                $('#documentationScopeName').html(structurizr.util.escapeHtml(structurizr.workspace.name));
            }
        }

        structurizr.workspace.model.softwareSystems.forEach(function(softwareSystem) {
            if (softwareSystem.documentation.sections.length > 0) {
                elementsWithDocumentation.push(softwareSystem.id);

                const scope = toScope(softwareSystem);
                if (requestedScope === scope) {
                    sections = softwareSystem.documentation.sections;
                    contentRenderer.setScope(softwareSystem);
                    $('#documentationScopeName').html(structurizr.util.escapeHtml(softwareSystem.name));
                }
            }

            if (softwareSystem.containers) {
                softwareSystem.containers.forEach(function(container) {
                    if (container.documentation.sections.length > 0) {
                        elementsWithDocumentation.push(container.id);

                        const scope = toScope(container);
                        if (requestedScope === scope) {
                            sections = container.documentation.sections;
                            contentRenderer.setScope(container);
                            $('#documentationScopeName').html(structurizr.util.escapeHtml(container.name));
                        }
                    }

                    if (container.components) {
                        container.components.forEach(function(component) {
                            if (component.documentation.sections.length > 0) {
                                elementsWithDocumentation.push(component.id);

                                const scope = toScope(component);
                                if (requestedScope === scope) {
                                    sections = component.documentation.sections;
                                    contentRenderer.setScope(component);
                                    $('#documentationScopeName').html(structurizr.util.escapeHtml(component.name));
                                }
                            }
                        });
                    }
                });
            }
        });

        if (sections.length > 0) {
            sections.sort(function (a, b) {
                return a.order - b.order;
            });

            // assign section numbers
            var sectionNumber = 1;
            sections.forEach(function(section) {
                section.number = sectionNumber++;
            })
        }
    }

    function toScope(element) {
        if (element.type === structurizr.constants.SOFTWARE_SYSTEM_ELEMENT_TYPE) {
            return element.name;
        } else if (element.type === structurizr.constants.CONTAINER_ELEMENT_TYPE) {
            const softwareSystem = structurizr.workspace.findElementById(element.parentId);
            return toScope(softwareSystem) + '/' + element.name;
        } else if (element.type === structurizr.constants.COMPONENT_ELEMENT_TYPE) {
            const container = structurizr.workspace.findElementById(element.parentId);
            return toScope(container) + '/' + element.name;
        }

        return undefined;
    }

    function scrollToHash() {
        var hash = window.location.hash;
        if (hash && hash.length > 0) {
            hash = hash.substring(1);
            const anchor = $('a[name="' + hash + '"]');
            if (anchor && anchor.length > 0) {
                anchor[0].scrollIntoView(true);
            }
        } else {
            $('#documentationScopeName')[0].scrollIntoView(false);
            documentationNavigationDropDown.val(decodeURIComponent(window.location.pathname));
        }
    }

    function showNoDocumentationPage() {
        const documentationContent = $('#documentationContent');
        $('#documentationScopeName').html(structurizr.util.escapeHtml(structurizr.workspace.name));
        documentationContent.addClass('centered');
        documentationContent.css('margin-top', '100px');
        documentationContent.css('margin-bottom', '100px');

        var random = Math.floor((Math.random() * 4) + 1);
        if (random === 1) {
            documentationContent.append('<p>The owner of this workspace values working software over comprehensive documentation. :-)</p>');
        } else if (random === 2) {
            documentationContent.append('<p>This page intentionally left blank. :-)</p>');
        } else if (random === 3) {
            documentationContent.append('<p>The code is the documentation. :-)</p>');
        } else {
            documentationContent.append("<p>This isn't the documentation you're looking for... :-)</p>");
        }

        documentationContent.append('<p class="smaller">(there is no documentation in this workspace)</p>');
    }

    function show() {
        $('#documentationContent').empty();

        if (requestedScope === WORKSPACE_SCOPE) {
            if (sections.length > 0) {
                sections.forEach(function(section) {
                    showDocumentationSection(section);
                });
            } else {
                // workspace level documentation has been requested, but none exists,
                // so show documentation for the first element instead
                const elementId = elementsWithDocumentation[0];
                const element = structurizr.workspace.findElementById(elementId);
                const scope = toScope(element);
                window.location.href = '<c:out value="${urlPrefix}" />/documentation/' + encodeURI(scope) + '<c:out value="${urlSuffix}" escapeXml="false" />';
                return;
            }
        } else {
            sections.forEach(function(section) {
                showDocumentationSection(section);
            });
        }

        rewriteInternalWorkspaceLinks();
        assignSectionNumbers();
        renderNavigation();

        $('#renderingModeLightLink').click(function(event) {
            event.preventDefault();
            structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_LIGHT);
            setDarkModeOnEmbeddedDiagrams();
        });

        $('#renderingModeDarkLink').click(function(event) {
            event.preventDefault();
            structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_DARK);
            setDarkModeOnEmbeddedDiagrams();
        });

        $('#renderingModeSystemLink').click(function(event) {
            event.preventDefault();
            structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_SYSTEM);
            setDarkModeOnEmbeddedDiagrams();
        });

        if (window.location.hash !== undefined) {
            scrollToHash();
        }

        $("#documentationContent a").each(function() {
            const a = $(this);
            const href = a.attr('href');

            if (href !== undefined) {
                if (href.indexOf('#') === -1) {
                    const targetSection = filenameToSection[href];
                    if (targetSection !== undefined) {
                        a.attr('href', '#' + targetSection.number)
                    }
                } else {
                    const parts = href.split('#');
                    const filename = parts[0];
                    const hash = parts[1];

                    const targetSection = filenameToSection[filename];
                    if (targetSection !== undefined) {
                        a.attr('href', '#' + hash);
                    }
                }
            }
        });

        $("#documentationContent img").click(function(){
            const image = $(this);
            const src = image.attr("src");

            const imageWindow = window.open("", "");
            imageWindow.document.write('<img src="' + src + '" />');
        });

        rendered = false;

        setTimeout(checkDiagramsRendered, 500);
    }

    function setDarkModeOnEmbeddedDiagrams() {
        const embeddedDiagrams = $('#documentationContent iframe.structurizrEmbed');
        embeddedDiagrams.each(function (index) {
            var iframe = embeddedDiagrams[index];
            if (iframe.contentWindow.structurizr && iframe.contentWindow.structurizr.diagram) {
                iframe.contentWindow.structurizr.diagram.setDarkMode(structurizr.ui.isDarkMode());
            }
        });
    }

    function renderNavigation() {
        elementsWithDocumentation.forEach(function(elementId) {
            var scope;

            if (elementId === WORKSPACE_SCOPE) {
                scope = WORKSPACE_SCOPE;
                const uri = '<c:out value="${urlPrefix}" />/documentation<c:out value="${urlSuffix}" escapeXml="false" />';

                documentationNavigation.append('<div class="documentationNavigationLink documentationNavigationHeading"><a href="' + uri + '">' + structurizr.util.escapeHtml(structurizr.workspace.name) + '</a></div>');
                documentationNavigationDropDown.append(
                    $('<option></option>').val(uri).html('[Workspace]' + structurizr.util.escapeHtml(structurizr.workspace.name))
                );
                quickNavigation.addItem('[Workspace] ' + structurizr.util.escapeHtml(structurizr.workspace.name), uri);
            } else {
                var element = structurizr.workspace.findElementById(elementId);
                if (element !== undefined) {
                    scope = toScope(element);
                    var uri = '<c:out value="${urlPrefix}" />/documentation/' + encodeURI(scope) + '<c:out value="${urlSuffix}" escapeXml="false" />';

                    documentationNavigation.append('<div class="documentationNavigationLink documentationNavigationHeading"><a href="' + uri + '">' + structurizr.util.escapeHtml(element.name) + '</a></div>');
                    documentationNavigationDropDown.append(
                        $('<option></option>').val(uri).html(structurizr.util.escapeHtml('[' + structurizr.workspace.getTerminologyFor(element)) + '] ' + structurizr.util.escapeHtml(element.name))
                    );
                    quickNavigation.addItem(structurizr.util.escapeHtml('[' + structurizr.workspace.getTerminologyFor(element)) + '] ' + structurizr.util.escapeHtml(element.name), uri);
                }
            }

            if (scope === requestedScope) {
                renderNavigationForScope();
            }
        });

        documentationNavigationDropDown.change(function() {
            window.location.href = documentationNavigationDropDown.val();
        });

        $('#exportLink').click(function(event) {
            event.preventDefault();
            exportDocumentationToOfflineHtmlPage();
        });
    }

    function renderNavigationForScope() {
        $('.documentationSectionContent h2, .documentationSectionContent h3').each(function () {
            const tag = $(this).prop("tagName");
            var sectionTitle = $(this).text();
            const sectionNumber = sectionTitle.trim().split(' ')[0];
            sectionTitle = sectionTitle.substr(sectionNumber.length + 1);

            var linkClass = 'documentationNavigationSection';
            var padding = '&nbsp;&nbsp;&nbsp;&nbsp;';

            if (tag === 'H3') {
                linkClass = 'documentationNavigationSubSection';
                padding = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
            }

            const sectionAnchorByName = '#' + findHeadingAnchor(sectionNumber, sectionTitle);
            anchors.push(sectionAnchorByName);
            navigationIdsByAnchor[sectionAnchorByName] = anchors.length;

            documentationNavigation.append('<div id="documentationNavigationLink' + (anchors.length) + '" class="documentationNavigationLink ' + linkClass + '"><a href="' + sectionAnchorByName + '">' + sectionNumber + ' ' + structurizr.util.escapeHtml(sectionTitle) + '</a></div>');
            documentationNavigationDropDown.append(
                $('<option class="sectionNavigationOption"></option>').val(sectionAnchorByName).html(padding + sectionNumber + ' ' + structurizr.util.escapeHtml(sectionTitle))
            );
            quickNavigation.addItem(padding + sectionNumber + ' ' + structurizr.util.escapeHtml(sectionTitle), sectionAnchorByName);
        });
    }

    function highlightNavigation(event, hash) {
        if (hash === undefined) {
            hash = location.hash;
        }

        $('.documentationNavigationLink').removeClass('documentationNavigationLinkActive');

        const index = navigationIdsByAnchor[hash];
        if (index === undefined && hash === '') {
            $('#documentationScopeName')[0].scrollIntoView(false);
        }

        if (index > -1) {
            $('#documentationNavigationLink' + index).addClass('documentationNavigationLinkActive');
            $('#documentationNavigationDropDown').val(hash);

            try {
                // scroll the navigation link into view if needed
                const documentationNavigationPanel = $('#documentationNavigationPanel');
                const navigationLink = $('#documentationNavigationLink' + index);
                if (navigationLink.offset().top < documentationNavigationPanel.offset().top) {
                    navigationLink[0].scrollIntoView(true);
                } else if ((navigationLink.offset().top + navigationLink.height()) > (documentationNavigationPanel.offset().top + documentationNavigationPanel.height())) {
                    navigationLink[0].scrollIntoView(false);
                }
            } catch (err) {
                console.log(err);
            }
        }
    }

    function checkDiagramsRendered() {
        var numberOfDiagramsRendered = 0;
        for (var i = 0; i < window.frames.length; i++) {
            const iframe = window.frames[i];

            if (iframe.isRendered !== undefined && iframe.isRendered())  {
                numberOfDiagramsRendered++;
            }
        }

        if (numberOfDiagramsRendered < window.frames.length) {
            setTimeout(checkDiagramsRendered, 500);
        } else {
            rendered = true;
            $('#exportLink').removeClass('hidden');
        }
    }

    function showDocumentationSection(section) {
        if (section) {
            if (section.filename) {
                filenameToSection[section.filename] = section;
            }

            const result = contentRenderer.render(section);
            const documentationContent = $('#documentationContent');

            var html = '';
            html += '<div class="documentationSection">';
            html += '<div class="documentationSectionContent">' + result + '</div>';
            html += '</div>';
            documentationContent.append(html);
        }

        return section;
    }

    function rewriteInternalWorkspaceLinks() {
        $('.documentationSectionContent a').each(function () {
            var href = $(this).attr('href');
            href = decodeURIComponent(href);
            if (href.indexOf(structurizr.constants.INTRA_WORKSPACE_URL_PREFIX) === 0) {
                // convert {workspace}/doc... to /workspace/1234/doc...
                href = '<c:out value="${urlPrefix}" />' + href.substring(structurizr.constants.INTRA_WORKSPACE_URL_PREFIX.length) + '<c:out value="${urlSuffix}" escapeXml="false" />';
                $(this).attr('href', href)
            }
        });
    }

    function assignSectionNumbers() {
        $('.documentationSectionContent h1').each(function () {
            $(this).remove();
        });

        var sectionNumber = 0;
        var subSectionNumber = 0;
        var subSubSectionNumber = 0;
        $('.documentationSectionContent h2, .documentationSectionContent h3, .documentationSectionContent h4').each(function () {
            var tag = $(this).prop("tagName");
            var text = $(this).text();
            var html = $(this).html();

            if (tag === "H2") {
                sectionNumber++;
                subSectionNumber = 0;
                subSubSectionNumber = 0;

                const sectionNumberToRender = sectionNumber;
                const sectionTitle = text;
                const headingAnchor = registerHeadingAnchor(sectionNumberToRender, sectionTitle);

                $('<a name="' + sectionNumberToRender + '"/><a name="' + headingAnchor + '" class="headingAnchor" />').insertBefore(this);
                $(this).html(sectionNumberToRender + ' ' + html);
            } else if (tag === "H3") {
                subSectionNumber++;
                subSubSectionNumber = 0;

                const sectionNumberToRender = sectionNumber + '.' + subSectionNumber;
                const sectionTitle = text;
                const headingAnchor = registerHeadingAnchor(sectionNumberToRender, sectionTitle);

                $('<a name="' + sectionNumberToRender + '"/><a name="' + headingAnchor + '" class="headingAnchor" />').insertBefore(this);
                $(this).html(sectionNumberToRender + ' ' + html);
            } else if (tag === "H4") {
                subSubSectionNumber++;

                const sectionNumberToRender = sectionNumber + '.' + subSectionNumber + '.' + subSubSectionNumber;
                const sectionTitle = text;
                const headingAnchor = registerHeadingAnchor(sectionNumberToRender, sectionTitle);

                $('<a name="' + sectionNumberToRender + '"/><a name="' + headingAnchor + '" class="headingAnchor" />').insertBefore(this);
                $(this).html(sectionNumberToRender + ' ' + html);
            }
        });

        $('.sectionSelector').addClass('hidden');
    }

    function changeDocumentation(evt) {
        const hash = window.location.hash;
        const index = anchors.indexOf(hash);

        if (evt.key === 'ArrowRight') {
            if (index < anchors.length - 1) {
                window.location.hash = anchors[index+1];
            }
        } else if (evt.key === 'ArrowLeft') {
            if (index > 0) {
                window.location.hash = anchors[index-1];
            }
        }
    }

    function resize() {
        const topNavigationHeight = $('#topNavigation').outerHeight(true);
        const padding = 20;
        var navigationDropDownHeight = 0;
        if (documentationNavigationDropDownPanel.is(':visible')) {
            navigationDropDownHeight = documentationNavigationDropDownPanel.innerHeight();
        }
        const documentationPanelHeight = (window.innerHeight - topNavigationHeight - padding - navigationDropDownHeight);
        $('#documentationPanel').css('height', documentationPanelHeight + 'px');

        $('.img-thumbnail').css('max-height', 0.8 * documentationPanelHeight);

        $('#documentationContent').css('margin-bottom', (0.75 * documentationPanelHeight) + 'px');
        $('#documentationNavigationPanel').css('height', (window.innerHeight - topNavigationHeight - padding) + 'px');
    }

    function exportDocumentationToOfflineHtmlPage(callback) {
        var exportWindow;
        if (structurizr.ui.isDarkMode()) {
            exportWindow = window.open('/static/html/offline-documentation-dark.html');
        } else {
            exportWindow = window.open('/static/html/offline-documentation-light.html');
        }

        const exportDocumentation = function() {
            const documentationContentDiv = exportWindow.document.getElementById('documentationContent');

            if (!documentationContentDiv) {
                setTimeout(exportDocumentation, 100);
            } else {
                var originalContent = document.getElementById("documentationContent").innerHTML;
                exportWindow.document.title = "Structurizr - " + structurizr.workspace.name;
                exportWindow.document.getElementById('brandingStyles').innerHTML = document.getElementById('brandingStyles').innerHTML;
                exportWindow.document.getElementById('documentationHeader').innerHTML = document.getElementById('documentationHeader').innerHTML;

                const embeddedDiagrams = $('#documentationContent iframe.structurizrEmbed');
                embeddedDiagrams.each(function(index) {
                    var iframe = embeddedDiagrams[index];
                    if (iframe.contentWindow.isExportable && iframe.contentWindow.isExportable()) {
                        if (iframe.contentWindow.structurizr.diagram.getCurrentViewOrFilter().type === structurizr.constants.IMAGE_VIEW_TYPE) {
                            var imageElement = exportWindow.document.createElement("img");
                            imageElement.className = 'img-thumbnail';
                            imageElement.style = 'max-width: 100%; height: auto;';

                            var content = iframe.contentWindow.structurizr.diagram.getCurrentViewOrFilter().content;
                            var parentDiv = iframe.parentNode;
                            imageElement.src = content;
                            parentDiv.insertBefore(imageElement, iframe);
                            parentDiv.removeChild(iframe);
                        } else {
                            var embeddedDiagramDiv = exportWindow.document.createElement("div");
                            embeddedDiagramDiv.className = 'img-thumbnail';

                            var svgMarkupForDiagram = iframe.contentWindow.structurizr.diagram.exportCurrentDiagramToSVG({ metadata: true, crop: false}).markup;
                            var svgMarkupForDiagramKey = iframe.contentWindow.structurizr.diagram.exportCurrentDiagramKeyToSVG();

                            var parentDiv = iframe.parentNode;
                            embeddedDiagramDiv.innerHTML = svgMarkupForDiagram + '<div class="diagramKey">' + svgMarkupForDiagramKey + '</div>';
                            parentDiv.insertBefore(embeddedDiagramDiv, iframe);
                            parentDiv.removeChild(iframe);
                        }
                    } else {
                        iframe.parentNode.removeChild(iframe);
                    }
                });

                documentationContentDiv.innerHTML = document.getElementById("documentationContent").innerHTML;

                if (callback) {
                    callback(exportWindow.document.documentElement.outerHTML);
                    exportWindow.close();
                } else {
                    const content = exportWindow.document.documentElement.outerHTML;
                    structurizr.util.downloadFile(content, "text/html;charset=utf-8", 'structurizr-' + structurizr.workspace.id + '-documentation.html');
                    exportWindow.close();
                }

                document.getElementById("documentationContent").innerHTML = originalContent;
            }
        };

        if (window.navigator.userAgent.indexOf("Edge") > -1) {
            exportDocumentation();
        } else {
            exportWindow.addEventListener("load", exportDocumentation);
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
