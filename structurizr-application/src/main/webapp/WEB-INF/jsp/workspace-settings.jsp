<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<div class="section">
    <div class="container centered">
        <h1><c:out value="${workspace.name}" escapeXml="true" /></h1>
        <p>
            <c:out value="${workspace.description}" escapeXml="true" />
        </p>
        <p class="smaller">
            (<a href="/workspace/${workspaceId}">back to workspace summary</a>)
        </p>

        <br />

        <div class="workspaceDetails centered">

            <div class="row workspaceDetailSection">
                <div class="col-6">
                    <h4>Client-side encryption <span class="smaller">(<a href="/help/client-side-encryption" target="_blank">help</a>)</span></h4>

                    <c:if test="${workspace.editable && not workspace.locked}">
                        <c:choose>
                            <c:when test="${workspace.clientEncrypted}">
                                <button id="removeClientSideEncryptionButton" class="btn btn-default small"><img src="/static/bootstrap-icons/file-earmark.svg" class="icon-btn" /> Remove client-side encryption</button>
                                <button id="addClientSideEncryptionButton" class="btn btn-default small"><img src="/static/bootstrap-icons/file-earmark-lock.svg" class="icon-btn" /> Change passphrase</button>
                            </c:when>
                            <c:otherwise>
                                <button id="addClientSideEncryptionButton" class="btn btn-default small"><img src="/static/bootstrap-icons/file-earmark-lock.svg" class="icon-btn" /> Add client-side encryption</button>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </div>

                <div class="col-6">
                    <c:if test="${authenticationEnabled}">
                    <h4>Role-based access</h4>

                    <div class="small" style="margin-top: 10px">
                        <button id="manageUsersButton" class="btn btn-default small"><img src="/static/bootstrap-icons/people.svg" class="icon-btn" /> Manage users</button>
                    </div>
                    </c:if>
                </div>
            </div>

            <div class="row workspaceDetailSection">
                <div class="col-6">
                    <c:if test="${workspace.editable}">
                        <a name="api"></a>
                        <h4>API details <span class="smaller">(<a href="https://structurizr.com/help/web-api" target="_blank">help</a>)</span></h4>
                        <p class="small">
                            Workspace ID: <span id="workspace${workspace.id}Id" style="font-family: 'Courier New', Courier, monospace; cursor: pointer">${workspace.id}</span>
                            <br />
                            API URL: <span id="workspace${workspace.id}ApiUrl" style="font-family: 'Courier New', Courier, monospace; cursor: pointer"><span class="baseUrl"></span>/api</span>
                            <br />
                            API key:
                            <span id="workspace${workspace.id}ApiKey" style="font-family: 'Courier New', Courier, monospace; cursor: pointer">${workspace.apiKey}</span>
                            <br />
                            API secret:
                            <span id="workspace${workspace.id}ApiSecret" style="font-family: 'Courier New', Courier, monospace; cursor: pointer">${workspace.apiSecret}</span>
                        </p>

                        <p class="small">
                            Structurizr CLI parameters <span class="smaller">(<a href="https://github.com/structurizr/cli/blob/master/docs/push.md" target="_blank">help</a>)</span>
                            <br />
                        <pre id="workspace${workspace.id}Cli" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">-url <span class="baseUrl"></span>/api -id ${workspace.id} -key ${workspace.apiKey} -secret ${workspace.apiSecret}</pre>
                        </p>
                    </c:if>
                </div>

                <div class="col-6">
                    <h4>Workspace visibility <span class="smaller">(<a href="https://structurizr.com/help/workspace-sharing" target="_blank">help</a>)</span></h4>

                    <c:if test="${not empty workspace.writeUsers}">
                    <c:if test="${workspace.publicWorkspace}">
                        <div class="small" style="margin-bottom: 5px">
                            <a href="/share/${workspace.id}" title="Sharing link">${structurizrConfiguration.webUrl}/share/${workspace.id}${urlSuffix}</a>
                        </div>
                    </c:if>

                    <c:if test="${workspace.editable}">
                        <div>
                            <c:choose>
                                <c:when test="${workspace.publicWorkspace}">
                                    <form id="privateWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/private" method="post">
                                        <input type="hidden" name="workspaceId" value="${workspace.id}" />
                                        <button class="btn btn-default small" type="submit" name="action" value="private" title="Make workspace private"><img src="/static/bootstrap-icons/lock.svg" class="icon-btn" /> Make private</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form id="publicWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/public" method="post">
                                        <input type="hidden" name="workspaceId" value="${workspace.id}" />
                                        <button class="btn btn-default small" type="submit" name="action" value="public" title="Make workspace public"><img src="/static/bootstrap-icons/unlock.svg" class="icon-btn" /> Make public</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>

                    <br />
                    </c:if>

                    <c:if test="${workspace.shareable}">
                        <div class="small" style="margin-bottom: 5px">
                            <a href="/share/${workspace.id}/${workspace.sharingToken}" title="Sharing link">${structurizrConfiguration.webUrl}/share/${workspace.id}/${workspace.sharingTokenTruncated}${urlSuffix}</a>
                        </div>
                    </c:if>

                    <c:if test="${workspace.editable}">
                        <div>
                            <c:choose>
                                <c:when test="${not empty workspace.sharingToken}">
                                    <form id="unshareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/unshare" method="post">
                                        <input type="hidden" name="workspaceId" value="${workspace.id}" />
                                        <button class="btn btn-default small" type="submit" name="action" value="unshare" title="Disable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn" /> Disable sharing link</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form id="shareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/share" method="post">
                                        <input type="hidden" name="workspaceId" value="${workspace.id}" />
                                        <button class="btn btn-default small" type="submit" name="action" value="share" title="Enable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn" /> Enable sharing link</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="row workspaceDetailSection">
                <div class="col-12">
                    <c:if test="${showAdminFeatures}">
                        <h4>Delete workspace</h4>
                        <p class="small">
                            Click the button below to delete your workspace. This action cannot be undone, and your workspace data will be irretrievable - we recommend exporting your workspace as a backup.
                        </p>
                        <button id="exportWorkspaceButton" class="btn btn-default small" title="Export workspace as JSON"><img src="/static/bootstrap-icons/filetype-json.svg" class="icon-btn" /> Export workspace</button>
                        <form id="deleteWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/delete" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <button class="btn btn-danger small" type="submit" name="action" value="remove" title="Delete workspace"><img src="/static/bootstrap-icons/folder-x.svg" class="icon-white icon-btn" /> Delete workspace</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>


<script nonce="${scriptNonce}">

    $('#workspace${workspace.id}Id').click(function() { structurizr.util.selectText('workspace${workspace.id}Id'); });
    $('#workspace${workspace.id}ApiUrl').click(function() { structurizr.util.selectText('workspace${workspace.id}ApiUrl'); });
    $('#workspace${workspace.id}ApiKey').click(function() { structurizr.util.selectText('workspace${workspace.id}ApiKey'); });
    $('#workspace${workspace.id}ApiSecret').click(function() { structurizr.util.selectText('workspace${workspace.id}ApiSecret'); });
    $('#workspace${workspace.id}Cli').click(function() { structurizr.util.selectText('workspace${workspace.id}Cli'); });

    $('#addClientSideEncryptionButton').click(function() { addClientSideEncryption(); });
    $('#removeClientSideEncryptionButton').click(function() { removeClientSideEncryption(); });
    $('#manageUsersButton').click(function() { window.location.href = '/workspace/${workspace.id}/users'; });

    $('#publicWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to make this workspace public?'); });
    $('#privateWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to make this workspace private?'); });
    $('#shareWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to share this workspace with a link?'); });
    $('#unshareWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to stop sharing this workspace with a link?'); });


    $('#exportWorkspaceButton').click(function() { structurizr.util.exportWorkspace(structurizr.workspace.id, structurizr.workspace.getJson()); });
    $('#deleteWorkspaceForm').on('submit', function() { return deleteWorkspace(); });

    function workspaceLoaded() {
        $('.baseUrl').text(window.location.protocol + '//' + window.location.host);
    }

    function deleteWorkspace() {
        if (confirm('Are you sure you want to delete this workspace?')) {
            try {
                structurizr.util.exportWorkspace(structurizr.workspace.id, structurizr.workspace.getJson())
                return true;
            } catch (e) {
                alert(e);
                return false;
            }
        }

        return false;
    }

    function addClientSideEncryption() {
        var passphrase1 = prompt("Please enter a passphrase to encrypt this workspace. Please note that if you lose this passphrase, your workspace will be irretrievable.");
        var passphrase2 = prompt("Please confirm your passphrase.");

        if (passphrase1 && passphrase1.trim().length > 0) {
            if (passphrase1 === passphrase2) {
                structurizrEncryptionStrategy = new structurizr.io.EncryptionStrategy({
                    type: "aes",
                    iterationCount: 1000,
                    keySize: 128,
                    passphrase: passphrase1
                });
                structurizr.saveWorkspace(function() {
                    const indexOfVersionParameter = window.location.href.indexOf('?version=');
                    if (indexOfVersionParameter > -1) {
                        window.location.href = window.location.href.substr(0, indexOfVersionParameter);
                    } else {
                        location.reload();
                    }
                });
            } else {
                alert('Passphrases do not match - please try again.');
            }
        }
    }

    function removeClientSideEncryption() {
        if (confirm('Are you sure you want to remove client-side encryption?')) {
            structurizrEncryptionStrategy = undefined;
            structurizr.saveWorkspace(function() {
                const indexOfVersionParameter = window.location.href.indexOf('?version=');
                if (indexOfVersionParameter > -1) {
                    window.location.href = window.location.href.substr(0, indexOfVersionParameter);
                } else {
                    location.reload();
                }
            });
        }
    }
</script>

<%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>