<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<div class="section">
    <div class="container centered">
        <h1><c:out value="${workspace.name}" escapeXml="true" /></h1>
        <p class="smaller">
            (<a href="/workspace/${workspaceId}">back to workspace summary</a>)
        </p>

        <br />

        <p>
            This page shows the set of users/roles that have access to this workspace.
            Having no users/roles defined on this page means that the workspace is accessible to any authenticated user.
            Users and roles can also be configured via the <a href="https://docs.structurizr.com/dsl/language#users" target="_blank">Structurizr DSL</a>.
        </p>

        <p class="small centered">
            <c:choose>
                <c:when test="${empty user.roles}">
                    (you are signed in as <code>${user.username}</code>, with no roles)
                </c:when>
                <c:otherwise>
                    (you are signed in as <code>${user.username}</code>, with roles
                    <c:forEach var="role" items="${user.roles}"> <code>${role}</code></c:forEach>)
                </c:otherwise>
            </c:choose>
        </p>

        <br />

        <c:choose>
            <c:when test="${workspace.editable}">
            <form action="/workspace/${workspace.id}/users" method="post">

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="writeUsersTextArea"><img src="/static/bootstrap-icons/pencil.svg" class="icon-sm" /> Read-write users and roles</label>
                            <textarea id="writeUsersTextArea" name="writeUsers" class="form-control" rows="10">${writeUsers}</textarea>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="readUsersTextArea"><img src="/static/bootstrap-icons/eye.svg" class="icon-sm" /> Read-only users and roles</label>
                            <textarea id="readUsersTextArea" name="readUsers" class="form-control" rows="10">${readUsers}</textarea>
                        </div>
                    </div>
                </div>

                <div class="centered">
                    <button type="submit" class="btn btn-primary">Update</button>
                </div>
                </form>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="writeUsersTextArea">Read-write users and roles</label>
                            <textarea id="writeUsersTextArea" name="writeUsers" class="form-control" rows="10" disabled="disabled">${writeUsers}</textarea>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="readUsersTextArea">Read-only users and roles</label>
                            <textarea id="readUsersTextArea" name="readUsers" class="form-control" rows="10" disabled="disabled">${readUsers}</textarea>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>


<script nonce="${scriptNonce}">
    function workspaceLoaded() {
        structurizr.ui.applyWorkspaceLogo();
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