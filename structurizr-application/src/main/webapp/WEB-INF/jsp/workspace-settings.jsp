<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<div class="section">
    <div class="container">
        <div class="centered">
            <h1><c:out value="${workspace.name}" escapeXml="true" /></h1>
            <p>
                <c:out value="${workspace.description}" escapeXml="true" />
            </p>
            <p class="smaller">
                (<a href="/workspace/${workspaceId}">back to workspace summary</a>)
            </p>
        </div>

        <br />

        <c:if test="${workspace.editable}">
        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Workspace API</h4>
                <p>
                    <a href="https://docs.structurizr.com/server/workspace-api" target="_blank">Documentation</a>
                </p>
            </div>
            <div class="col-10">
                <p>
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

                <p>
                    Structurizr parameters for <a href="https://docs.structurizr.com/push" target="_blank">push</a> and <a href="https://docs.structurizr.com/push" target="_blank">pull</a> via the web API</span>
                    <br />
                    <pre id="workspace${workspace.id}Cli" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">-url <span class="baseUrl"></span>/api -id ${workspace.id} -key ${workspace.apiKey} -secret ${workspace.apiSecret}</pre>
                </p>
            </div>
        </div>
        <hr />
        </c:if>

        <c:if test="${showAdminFeatures}">
        <c:if test="${structurizrConfiguration.authenticationEnabled}">
        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Workspace visibility</h4>
            </div>
            <div class="col-10">
                <p>
                    Private URL: <a href="/workspace/${workspace.id}" target="_blank">/workspace/${workspace.id}</a>
                    <br />
                    <c:choose>
                    <c:when test="${workspace.shareable}">
                    Sharing URL: <a href="/share/${workspace.id}/${workspace.sharingToken}" target="_blank">/share/${workspace.id}/${workspace.sharingToken}</a>
                    </c:when>
                    <c:otherwise>
                    Sharing URL: -
                    </c:otherwise>
                    </c:choose>
                    <br />
                    <c:choose>
                    <c:when test="${workspace.publicWorkspace}">
                    Public URL: <a href="/share/${workspace.id}" target="_blank">/share/${workspace.id}</a>
                    </c:when>
                    <c:otherwise>
                    Public URL: -
                    </c:otherwise>
                    </c:choose>
                </p>

                <c:choose>
                    <c:when test="${workspace.publicWorkspace}">
                        <form id="privateWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="private" />
                            <button class="btn btn-primary" type="submit" name="action" value="private" title="Make workspace private"><img src="/static/bootstrap-icons/lock.svg" class="icon-btn icon-white" /> Make private</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form id="publicWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="public" />
                            <button class="btn btn-danger" type="submit" name="action" value="public" title="Make workspace public"><img src="/static/bootstrap-icons/unlock.svg" class="icon-btn icon-white" /> Make public</button>
                        </form>
                    </c:otherwise>
                </c:choose>

                <c:if test="${workspace.shareable}">
                    <div class="small" style="margin-bottom: 5px">
                        <a href="/share/${workspace.id}/${workspace.sharingToken}" title="Sharing link">${structurizrConfiguration.webUrl}/share/${workspace.id}/${workspace.sharingTokenTruncated}${urlSuffix}</a>
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${not empty workspace.sharingToken}">
                        <form id="unshareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="unshare" />
                            <button class="btn btn-primary" type="submit" name="action" value="unshare" title="Disable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn icon-white" /> Disable sharing link</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form id="shareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="share" />
                            <button class="btn btn-warning" type="submit" name="action" value="share" title="Enable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn" /> Enable sharing link</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <hr />
        </c:if>

        <c:if test="${workspace.editable && not workspace.locked}">
        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Client-side encryption</h4>
                <p>
                    <a href="https://docs.structurizr.com/server/client-side-encryption" target="_blank">Documentation</a>
                </p>
            </div>
            <div class="col-10">
                <c:choose>
                <c:when test="${workspace.clientEncrypted}">
                    <p>
                    Workspace is client-side encrypted.
                    </p>
                    <button id="removeClientSideEncryptionButton" class="btn btn-primary"><img src="/static/bootstrap-icons/file-earmark.svg" class="icon-btn icon-white" /> Remove client-side encryption</button>
                    <button id="addClientSideEncryptionButton" class="btn btn-primary"><img src="/static/bootstrap-icons/file-earmark-lock.svg" class="icon-btn icon-white" /> Change passphrase</button>
                </c:when>
                <c:otherwise>
                    <button id="addClientSideEncryptionButton" class="btn btn-primary"><img src="/static/bootstrap-icons/file-earmark-lock.svg" class="icon-btn icon-white" /> Add client-side encryption</button>
                </c:otherwise>
                </c:choose>
            </div>
        </div>
        <hr />
        </c:if>

        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Delete workspace</h4>
            </div>
            <div class="col-10">
                <p>
                    Click the button below to delete your workspace - this action cannot be undone, and your workspace data will be irretrievable.
                </p>
                <form id="deleteWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/delete" method="post">
                    <input type="hidden" name="workspaceId" value="${workspace.id}" />
                    <button class="btn btn-danger small" type="submit" name="action" value="remove" title="Delete workspace"><img src="/static/bootstrap-icons/folder-x.svg" class="icon-white icon-btn" /> Delete workspace</button>
                </form>
            </div>
        </div>
        <hr />

        </c:if>

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