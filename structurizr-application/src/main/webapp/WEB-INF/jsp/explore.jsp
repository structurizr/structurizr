<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<div class="section">
    <div class="container centered">
        <h1>Explore</h1>

        <a href="${urlPrefix}/explore/model<c:out value="${urlSuffix}" escapeXml="false" />">Model</a>
        <br />
        <br />

        <div id="views" class="centered">
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    progressMessage.show('<p>Loading workspace...</p>');

    function workspaceLoaded() {
        var viewsDiv = $('#views');

        if (structurizr.workspace.hasViews()) {
            var views = structurizr.workspace.getViews();
            const thumbnailSize = 200;
            var html = '';

            views.forEach(function(view) {
                var diagramUrl = '<c:out value="${urlPrefix}" />/diagrams<c:out value="${urlSuffix}" escapeXml="false" />#' + structurizr.util.escapeHtml(view.key);
                var graphUrl;
                var treeUrl;

                if (
                    view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                    view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                    view.type === structurizr.constants.COMPONENT_VIEW_TYPE ||
                    view.type === structurizr.constants.DEPLOYMENT_VIEW_TYPE) {
                    treeUrl = '<c:out value="${urlPrefix}" />/explore/tree<c:out value="${urlSuffix}" escapeXml="false" />#' + structurizr.util.escapeHtml(view.key);
                }

                if (
                    view.type === structurizr.constants.CUSTOM_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_LANDSCAPE_VIEW_TYPE ||
                    view.type === structurizr.constants.SYSTEM_CONTEXT_VIEW_TYPE ||
                    view.type === structurizr.constants.CONTAINER_VIEW_TYPE ||
                    view.type === structurizr.constants.COMPONENT_VIEW_TYPE) {
                    graphUrl = '<c:out value="${urlPrefix}" />/explore/graph<c:out value="${urlSuffix}" escapeXml="false" />#' + structurizr.util.escapeHtml(view.key);
                }

                if (graphUrl !== undefined || treeUrl !== undefined) {
                    const title = structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view));

                    html += '<div class="centered" style="display: inline-block; margin: 10px 10px 40px 10px; width: ' + thumbnailSize + 'px;">';

                    <c:choose>
                    <c:when test="${not empty param.version}">
                    html += '  <a href="' + diagramUrl + '"><img src="/static/img/thumbnail-not-available.png" class="img-thumbnail" style="margin-bottom: 10px" /></a>';
                    </c:when>
                    <c:otherwise>
                    html += '  <a href="' + diagramUrl + '"><img src="${thumbnailUrl}' + structurizr.util.escapeHtml(view.key) + '-thumbnail.png" class="img-thumbnail viewThumbnail" style="margin-bottom: 10px; max-height: ' + thumbnailSize + 'px" /></a>';
                    </c:otherwise>
                    </c:choose>

                    html += '  <div class="smaller">';
                    html += '    ' + title + '<br />';
                    html += '    #' + structurizr.util.escapeHtml(view.key) + '';
                    html += '  </div>';
                    html += '  <div>';
                    if (graphUrl !== undefined) {
                        html += '    <a href="' + graphUrl + '">Graph</a>';
                    }
                    if (graphUrl !== undefined && treeUrl !== undefined) {
                        html += '    |';
                    }
                    if (treeUrl !== undefined) {
                        html += '    <a href="' + treeUrl + '">Tree</a>';
                    }
                    html += '  </div>';
                    html += '</div>';
                }
            });

            viewsDiv.addClass('centered');
            viewsDiv.append(html);

            $('.viewThumbnail').on('error', function() {
                $(this).on('error', undefined);
                $(this).attr('src', '/static/img/thumbnail-not-available.png');
            });
        }
    }

    $('#brandingLogoAnchor').attr('href', '<c:out value="${urlPrefix}" />');
    progressMessage.hide();
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