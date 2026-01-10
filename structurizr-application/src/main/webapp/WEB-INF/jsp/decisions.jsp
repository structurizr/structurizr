<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<script type="text/javascript" src="<c:url value="/static/js/structurizr-embed.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/structurizr-content.js" />"></script>
<script type="text/javascript" src="/static/js/markdown-it-13.0.1.min.js"></script>
<script type="text/javascript" src="/static/js/katex-0.16.4.min.js"></script>
<script type="text/javascript" src="/static/js/asciidoctor-2.2.6.min.js"></script>

<link href="/static/css/katex-0.16.4.min.css" rel="stylesheet" media="screen" />
<link href="<c:url value="/static/css/structurizr-asciidoctor.css" />" rel="stylesheet" media="screen" />
<link href="<c:url value="/static/css/structurizr-decisions.css" />" rel="stylesheet" media="screen" />

<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<script type="text/javascript" src="/static/js/d3-7.8.2.min.js"></script>

<style id="brandingStyles"></style>

<div class="row" style="padding-top: 20px; padding-bottom: 0; margin-left: 0; margin-right: 0">

    <div class="col-sm-3" style="padding-left: 30px">
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

            <div class="navigationItem">
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

    <div class="col-sm-9" style="margin-top: 0px; margin-bottom: 0px">
        <div class="centered d-sm-none">
            <div class="form-inline" style="margin-top: 20px">
                <select id="decisionLogNavigationDropDown" class="form-select">
                </select>
            </div>
        </div>

        <div id="documentationPanel">
            <div id="decisionLogHeader" style="border-bottom: none">
                <h1 id="decisionTitle" class="centered"></h1>
                <div id="decisionDate" class="centered"></div>
                <div class="centered" style="margin-top: 10px">
                    <span id="decisionStatus" class="centered hidden"></span>
                    <button id="graphButton" type="button" class="btn btn-default hidden" style="height: 42px; margin-bottom: 8px;"><img src="/static/bootstrap-icons/diagram-2.svg" class="icon-btn" /> Decision explorer</button>
                </div>
            </div>

            <div id="decisionLogContent"></div>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    const requestedScope = structurizr.util.atob('<c:out value="${scope}" />');

    $('#graphButton').click(function() { openGraphModal(); });

    const elementsWithDecisions = [];
    var decisions = [];
    const anchors = [];

    var contentRenderer;
    var graph;
    const documentationNavigation = $('#documentationNavigation');
    const decisionLogNavigationDropDown = $('#decisionLogNavigationDropDown');
    const documentationNavigationPanel = $('#documentationNavigationPanel');

    const defaultDecisionBackgrounds = {
        'proposed': '#5bc0de',
        'accepted': '#5cb85c',
        'superseded': '#f0ad4e',
        'deprecated': '#f0ad4e',
        'rejected': '#d9534f'
    };

    progressMessage.show('<p>Loading workspace...</p>');

    function workspaceLoaded() {
        init();
    }

    function init() {
        if (structurizr.workspace.hasDecisions()) {
            contentRenderer = new structurizr.ui.ContentRenderer(
                structurizr.workspace,
                '/static',
                '<c:out value="${urlPrefix}" />',
                ${structurizrConfiguration.safeMode});

            initDecisionScopeAndOrder();
            renderNavigation();

            $('#renderingModeLightLink').click(function(event) {
                event.preventDefault();
                structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_LIGHT);
            });

            $('#renderingModeDarkLink').click(function(event) {
                event.preventDefault();
                structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_DARK);
            });

            $('#renderingModeSystemLink').click(function(event) {
                event.preventDefault();
                structurizr.ui.setRenderingMode(structurizr.ui.RENDERING_MODE_SYSTEM);
            });

            show();

            window.onhashchange = show;

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
            showNoDecisionsPage();
        }

        structurizr.ui.applyBranding();
        $('#brandingLogoAnchor').attr('href', '<c:out value="${urlPrefix}" /><c:out value="${urSuffix}" />');
        resize();
        progressMessage.hide();
    }

    function initDecisionScopeAndOrder() {
        if (structurizr.workspace.documentation.decisions.length > 0) {
            const elementId = '*';
            if (elementsWithDecisions.indexOf(elementId) === -1) {
                elementsWithDecisions.push(elementId);
            }

            if (requestedScope === '*') {
                decisions = structurizr.workspace.documentation.decisions;
            }
        }

        structurizr.workspace.model.softwareSystems.forEach(function(softwareSystem) {
            if (softwareSystem.documentation.decisions.length > 0) {
                elementsWithDecisions.push(softwareSystem.id);

                var scope = toScope(softwareSystem);
                if (requestedScope === scope) {
                    decisions = softwareSystem.documentation.decisions;
                    contentRenderer.setScope(softwareSystem);
                }
            }

            if (softwareSystem.containers) {
                softwareSystem.containers.forEach(function(container) {
                    if (container.documentation.decisions.length > 0) {
                        elementsWithDecisions.push(container.id);

                        const scope = toScope(container);
                        if (requestedScope === scope) {
                            decisions = container.documentation.decisions;
                            contentRenderer.setScope(container);
                        }
                    }

                    if (container.components) {
                        container.components.forEach(function(component) {
                            if (component.documentation.decisions.length > 0) {
                                elementsWithDecisions.push(component.id);

                                const scope = toScope(component);
                                if (requestedScope === scope) {
                                    decisions = component.documentation.decisions;
                                    contentRenderer.setScope(component);
                                }
                            }
                        });
                    }
                });
            }
        });

        if (decisions && decisions.length > 0) {
            decisions.sort(function(a,b) {
                if (a.date !== undefined && b.date !== undefined) {
                    const result = b.date.localeCompare(a.date);

                    if (result === 0) {
                        return b.id - a.id;
                    } else {
                        return result;
                    }
                } else {
                    return b.id - a.id;
                }
            });
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

    function createDecisionAnchor(decision) {
        return '#' + decision.id;
    }

    function renderNavigation() {
        elementsWithDecisions.forEach(function(elementId) {
            var scope;

            if (elementId === '*') {
                scope = '*';
                const uri = '<c:out value="${urlPrefix}" />/decisions<c:out value="${urlSuffix}" escapeXml="false" />';
                documentationNavigation.append('<div class="decisionNavigationLink decisionNavigationHeading"><a href="' + uri + '">' + structurizr.util.escapeHtml(structurizr.workspace.name) + '</a></div>');
                decisionLogNavigationDropDown.append(
                    $('<option></option>').val(uri).html('Workspace] ' + structurizr.util.escapeHtml(structurizr.workspace.name))
                );
                quickNavigation.addItem(structurizr.util.escapeHtml('Workspace] ' + structurizr.workspace.name), uri);
            } else {
                var element = structurizr.workspace.findElementById(elementId);
                scope = toScope(element);
                const uri = '<c:out value="${urlPrefix}" />/decisions/' + encodeURI(scope) + '<c:out value="${urlSuffix}" escapeXml="false" />';

                documentationNavigation.append('<div class="decisionNavigationLink decisionNavigationHeading"><a href="' + uri + '">' + structurizr.util.escapeHtml(element.name) + '</a></div>');
                decisionLogNavigationDropDown.append(
                    $('<option></option>').val(uri).html("[" + structurizr.util.escapeHtml(structurizr.workspace.getTerminologyFor(element)) + "] " + structurizr.util.escapeHtml(element.name))
                );
                quickNavigation.addItem("[" + structurizr.util.escapeHtml(structurizr.workspace.getTerminologyFor(element)) + "] " + structurizr.util.escapeHtml(element.name), uri);
            }

            if (scope === requestedScope) {
                // render decision navigation
                decisions.forEach(function(decision) {
                    const decisionAnchor = createDecisionAnchor(decision);
                    anchors.push(decisionAnchor);

                    documentationNavigation.append('<div id="decisionNavigationLink' + (anchors.length-1) + '" class="decisionNavigationLink decisionNavigationItem"><a href="' + decisionAnchor + '">' + decision.id + '. ' + structurizr.util.escapeHtml(decision.title) + '</a></div>');
                    decisionLogNavigationDropDown.append(
                        $('<option>').val(decisionAnchor).html('&nbsp;&nbsp;' + decision.id + '. ' + structurizr.util.escapeHtml(decision.title))
                    );
                    quickNavigation.addItem(createStatusLabel(decision, true) + ' ' + decision.id + '. ' + structurizr.util.escapeHtml(decision.title), decisionAnchor);
                });
            }
        });

        decisionLogNavigationDropDown.change(function() {
            window.location.href = decisionLogNavigationDropDown.val();
        });
    }

    function highlightNavigation() {
        $('.decisionNavigationLink').removeClass('documentationNavigationLinkActive');
        const hash = window.location.hash;

        if (hash && hash.length > 0) {
            const index = anchors.indexOf(hash);
            var navigationLink = $('#decisionNavigationLink' + index);
            navigationLink.addClass('documentationNavigationLinkActive');
            decisionLogNavigationDropDown.val(hash);

            try {
                // scroll the navigation link into view if needed
                if (navigationLink.offset().top < documentationNavigationPanel.offset().top) {
                    navigationLink[0].scrollIntoView(true);
                } else if ((navigationLink.offset().top + navigationLink.height()) > (documentationNavigationPanel.offset().top + documentationNavigationPanel.height())) {
                    navigationLink[0].scrollIntoView(false);
                }
            } catch (err) {
                console.log(err);
            }
        } else {
            decisionLogNavigationDropDown.val(decodeURIComponent(window.location.pathname));
        }
    }

    function showNoDecisionsPage() {
        const decisionLogContent = $('#decisionLogContent');
        $('#decisionTitle').addClass('hidden');
        $('#decisionDate').addClass('hidden');
        $('#decisionStatus').addClass('hidden');

        decisionLogContent.addClass('centered');
        decisionLogContent.css('margin-top', '100px');
        decisionLogContent.css('margin-bottom', '100px');

        decisionLogContent.append('<p>There are no architecture decision records in this workspace.</p>');
    }

    function show() {
        $('#decisionLogContent').empty();
        document.getElementById('documentationPanel').scrollTo(0, 0);

        const hash = window.location.hash;
        var decisionId;
        if (hash && hash.length > 0) {
            decisionId = hash.substring(1); // remove the # symbol
        }

        if (graph === undefined && window.createGraph) {
            try {
                graph = createGraph();
                setWidthAndHeight();
                if (graph) {
                    renderGraph();
                }
            } catch (err) {
                console.log(err);
            }
        }

        if (decisionId === undefined) {
            showDecisionSummary();
        } else {
            showDecision(decisionId);
        }
    }

    function showDecisionSummary() {
        const decisionLogContent = $('#decisionLogContent');
        $('#decisionDate').addClass('hidden');
        $('#decisionStatus').addClass('hidden');

        var html = '';
        html += '<div class="section">';
        html += '<div class="decisionLogSummary">';

        if (requestedScope === '*') {
            if (decisions && decisions.length > 0) {
                $('#decisionTitle').html('Summary');
            } else if (elementsWithDecisions.length > 0) {
                // workspace level decisions has been requested, but none exist,
                // so show decisions for the first element instead
                const elementId = elementsWithDecisions[0];
                const element = structurizr.workspace.findElementById(elementId);
                const scope = toScope(element);
                window.location.href = '<c:out value="${urlPrefix}" />/decisions/' + encodeURI(scope) + '<c:out value="${urlSuffix}" escapeXml="false" />';
                return;
            } else {
                showNoDecisionsPage();
            }
        } else {
            $('#decisionTitle').html('Summary');
        }

        const years = [];
        var currentYear = undefined;
        decisions.forEach(function(decision) {
            var date = new Date(decision.date);

            if (currentYear !== date.getFullYear()) {
                if (currentYear !== undefined) {
                    html += '</table></div>';
                }

                currentYear = date.getFullYear();
                years.push(currentYear);

                html += '<div class="decisionTimelineYear">';
                html += currentYear;
                html += '</div>';

                html += '<div><table class="table table-striped" width="100%">';
            }

            html += '<tr>';
            html += '<td width="80%"><a href="' + createDecisionAnchor(decision) + '">' + decision.id + '. ' + structurizr.util.escapeHtml(decision.title) + '</a><div class="decisionSummaryMetadata">' + formatDate(decision.date) + '</div></td>';
            html += '<td style="text-align: right">' + createStatusLabel(decision, true) + '</td>';
            html += '</tr>';
        });

        html += '</table></div>';

        decisionLogContent.append(html);

        $('.decisionLogStatus').addClass('otherStatus');
        highlightNavigation();
    }

    function formatDate(dateAsString) {
        const timezone = structurizr.workspace.views.configuration.properties['structurizr.timezone'];
        const locale = structurizr.workspace.views.configuration.properties['structurizr.locale'];

        return new Date(dateAsString).toLocaleDateString(
            locale,
            {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                timeZone: timezone
            });
    }

    function createStatusLabel(decision, includeText) {
        const style = getDecisionStyle(decision);

        return '<span class="label" style="background: ' + style.background + '; color: ' + style.color + '">' + (includeText ? ' ' + structurizr.util.escapeHtml(decision.status) : '') + '</span>';
    }

    function showDecision(decisionId) {
        const decision = decisions.filter(function(d) { return d.id === decisionId; })[0];
        if (decision) {
            $('#decisionTitle').html(decision.id + ". " + structurizr.util.escapeHtml(decision.title));
            $('#decisionDate').html(formatDate(decision.date));
            $('#decisionDate').removeClass('hidden');
            $('#decisionStatus').html(createStatusLabel(decision, true));
            $('#decisionStatus').removeClass('hidden');

            const decisionLogContent = $('#decisionLogContent');
            const result = contentRenderer.render(decision);
            const html = '<div>' + result + '</div>';
            decisionLogContent.append(html);

            $('#decisionLogContent h1').each(function () {
                $(this).remove();
            });

            if (graph) {
                highlightDecisionInGraph(decision);
            }

            rewriteInternalWorkspaceLinks();
            highlightNavigation();
        }
    }

    function rewriteInternalWorkspaceLinks() {
        $('#decisionLogContent a').each(function () {
            var href = $(this).attr('href');
            href = decodeURIComponent(href);
            if (href.indexOf(structurizr.constants.INTRA_WORKSPACE_URL_PREFIX) === 0) {
                // convert {workspace}/doc... to /workspace/1234/doc...
                href = '<c:out value="${urlPrefix}" />' + href.substring(structurizr.constants.INTRA_WORKSPACE_URL_PREFIX.length) + '<c:out value="${urlSuffix}" escapeXml="false" />';
                $(this).attr('href', href)
            }
        });
    }

    function changeDecision(evt) {
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

    function getDecisionStyle(decision) {
        const status = decision.status;

        var background = defaultDecisionBackgrounds[status.toLowerCase()];
        if (background === undefined) {
            background = '#777777';
        }
        var color = '#ffffff';

        const style = structurizr.workspace.findElementStyleByTag("Decision:" + status);
        if (style) {
            if (style.background) {
                background = style.background;
            }
            if (style.color) {
                color = style.color;
            }
        }

        return {
            background: background,
            color: color
        };
    }

    function resize() {
        var topNavigationHeight = $('#topNavigation').outerHeight(true);
        var padding = 20;
        const contentHeight = (window.innerHeight - topNavigationHeight - padding);
        $('#documentationPanel').css('height', contentHeight + 'px');
        documentationNavigationPanel.css('height', contentHeight + 'px');

        structurizr.embed.setMaxHeight(0.8 * contentHeight);
        $('.img-thumbnail').css('max-height', 0.8 * contentHeight);

        if (graph !== undefined && $('#graphContent').is(':visible')) {
            resizeGraph();
        }
    }

    document.addEventListener("keydown", changeDecision);

    $(window).resize(function() {
        resize();
    });
</script>

<%@ include file="/WEB-INF/fragments/decisions/graph.jspf" %>

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