<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<div class="section">
    <div class="container centered">
        <h1>Images</h1>
        <p>
            Structurizr supports the ability to embed diagrams via a static PNG/SVG file via a regular image tag in HTML, Markdown, AsciiDoc, etc.
            <br />
            See <a href="https://docs.structurizr.com/server/image-embed" target="_blank">server - Image embed</a> for more details.
        </p>

        <br />
        <br />

        <c:choose>
        <c:when test="${not empty images}">
        <div class="centered">
        <c:forEach var="image" items="${images}">
            <div style="display: inline-block; margin: 10px 10px 40px 10px; max-width: 200px;">
                <a href="${image.url}" target="_blank"><img src="${image.url}" class="img-thumbnail" width="200px" /></a>
                <p class="smaller">
                    <b>${image.name}</b>
                    <br />
                    <fmt:formatDate value="${image.lastModifiedDate}" pattern="EEE dd MMM yyyy HH:mm z" timeZone="${user.timeZone}" />
                    <br />
                    ${image.sizeInKB} KB
                </p>
            </div>
        </c:forEach>
        </div>

        <c:if test="${workspace.editable}">
        <div class="centered">
            <form id="deleteImagesForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/images/delete" method="post">
                <button class="btn btn-danger" type="submit" name="action" value="delete" title="Delete published images"><img src="/static/bootstrap-icons/trash3.svg" class="icon-btn icon-white" /> Delete published images</button>
            </form>
        </div>
        </c:if>

        </c:when>
            <c:otherwise>
        <p class="centered" style="margin-top: 40px">
            <img src="/static/bootstrap-icons/info-circle.svg" class="icon-md" />
            No images have been published for this workspace.
        </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script nonce="${scriptNonce}">
    function workspaceLoaded() {
        structurizr.ui.applyWorkspaceLogo();
    }

    $('.viewThumbnail').on('error', function() {
        $(this).on('error', undefined);
        $(this).attr('src', '/static/img/thumbnail-not-available.png');
    });

    $('#deleteImagesForm').on('submit', function() {
        return confirm('Are you sure you want to delete the published images?');
    });
</script>

<c:choose>
    <c:when test="${not empty workspaceAsJson}">
<%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
<%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>