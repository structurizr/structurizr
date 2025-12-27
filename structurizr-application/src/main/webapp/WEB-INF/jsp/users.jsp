<div class="section">
    <div class="container centered">
        <h1><c:out value="${workspace.name}" escapeXml="true" /></h1>
        <p class="smaller">
            (<a href="/workspace/${workspaceId}">back to workspace summary</a>)
        </p>

        <br />

        <p class="centered" style="font-size: 20px">
            <c:choose>
            <c:when test="${empty workspace.writeUsers}">
            <img src="/static/bootstrap-icons/unlock.svg" class="icon-md" /> Public <span class="smaller">(role-based access is not active)</span>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${workspace.publicWorkspace}">
                    <img src="/static/bootstrap-icons/unlock.svg" class="icon-md" /> Public <span class="smaller">(role-based access is active and <a href="/workspace/${workspaceId}/settings">the workspace is marked as public</a>)</span>
                    </c:when>
                    <c:otherwise>
                    <img src="/static/bootstrap-icons/lock.svg" class="icon-md" /> Private <span class="smaller">(role-based access is active and <a href="/workspace/${workspaceId}/settings">the workspace is marked as private</a>)</span>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
            </c:choose>
        </p>

        <p>
            This page shows the set of users/roles that have access to this workspace.
            Having no users/roles defined on this page means that the workspace is public, and accessible to anybody who has access to your Structurizr installation.
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
                            <label for="writeUsersTextArea">Read-write users and roles</label>
                            <textarea id="writeUsersTextArea" name="writeUsers" class="form-control" rows="10">${writeUsers}</textarea>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="readUsersTextArea">Read-only users and roles</label>
                            <textarea id="readUsersTextArea" name="readUsers" class="form-control" rows="10">${readUsers}</textarea>
                        </div>
                    </div>
                </div>

                <div class="centered">
                    <button type="submit" class="btn btn-default">Update</button>
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