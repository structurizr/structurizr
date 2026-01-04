<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<style>
    .workspaceSummary {
        position: relative;
        display: inline-block;
        margin: 10px;
        font-size: 18px;
        max-width: 320px;
        min-height: 320px;
        max-height: 320px;
        height: 320px;
        padding: 10px;
        border:1px solid #ddd;
        border-radius: 4px;
        overflow-y: hidden;
    }

    .workspaceThumbnail {
        display: inline-block;
        width: 300px;
        height: 200px;
        min-height: 200px;
        max-height: 200px;
        overflow-y: hidden;
        margin-top: 10px;
    }
</style>

<div id="dashboard" class="section" style="padding-bottom: 0px">
    <div class="centered">

        <div style="margin-bottom: 20px">
            <c:if test="${numberOfWorkspaces > 0}">
            <a href="?sort=name&pageSize=${pageSize}" style="color: #444444;<c:if test="${sort eq 'name'}"> font-weight: bold;</c:if>">Name</a>
            <img src="/static/bootstrap-icons/sort-down.svg" class="icon-sm" />
            <a href="?sort=date&pageSize=${pageSize}" style="color: #444444;<c:if test="${sort eq 'date'}"> font-weight: bold;</c:if>">Date</a>
            </c:if>

            <c:if test="${not empty pageNumber}">
                <span style="padding-left: 20px; padding-right: 20px">|</span>
                <%@ include file="/WEB-INF/fragments/workspaces-page-control.jspf" %>
            </c:if>
        </div>

        <c:forEach var="workspace" items="${workspaces}" varStatus="status">
            <div class="workspaceSummary centered <c:if test="${not workspace.active}">inactive</c:if>">
                <div>
                    <a href="${workspace.urlPrefix}/${workspace.id}"><c:out value="${workspace.name}" escapeXml="true" /></a>
                </div>

                <div style="margin-top: 10px; margin-bottom: 10px; font-size: 11px">
                    <c:out value="${workspace.description}" escapeXml="true" />
                </div>

                <div class="workspaceThumbnail">
                    <a href="${workspace.urlPrefix}/${workspace.id}">
                    <img src="${workspace.urlPrefix}/${workspace.id}/images/thumbnail.png" alt="Thumbnail" class="img-light img-fluid workspaceThumbnailImage" />
                    <img src="${workspace.urlPrefix}/${workspace.id}/images/thumbnail-dark.png" alt="Thumbnail" class="img-dark img-fluid workspaceThumbnailImage" />
                    </a>
                </div>
            </div>
        </c:forEach>

        <c:if test="${userCanCreateWorkspace}">
            <div class="workspaceSummary centered">
                <div>
                    New workspace
                </div>

                <br /><br /><br />

                <div>
                    <div class="workspaceThumbnail" style="padding-top: 30px">
                        <a href="/workspace/create"><img src="/static/bootstrap-icons/folder-plus.svg" class="icon-xxl" /></a>
                    </div>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty pageNumber}">
        <div style="margin-top: 20px">
            <%@ include file="/WEB-INF/fragments/workspaces-page-control.jspf" %>
            <span style="padding-left: 20px; padding-right: 20px">|</span>
            Page size:
            <a href="?sort=${sort}&pageNumber=1&pageSize=10">10</a>
            |
            <a href="?sort=${sort}&pageNumber=1&pageSize=20">20</a>
            |
            <a href="?sort=${sort}&pageNumber=1&pageSize=50">50</a>
        </div>
        </c:if>
    </div>
</div>

<script nonce="${scriptNonce}">
    $('.workspaceThumbnailImage').on('error', function() {
        $(this).on('error', undefined);
        $(this).attr('src', '/static/img/thumbnail-not-available.png');
    });

    <c:forEach var="workspace" items="${workspaces}">
    quickNavigation.addItem('${workspace.id} - <c:out value="${workspace.name}" escapeXml="true" />', '${workspace.urlPrefix}/${workspace.id}');
    </c:forEach>
</script>