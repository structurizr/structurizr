<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<style>
    hr {
        margin-top: 60px;
    }
</style>

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
                    <c:choose>
                    <c:when test="${apiKeyRequired}">
                    API key: <span style="font-family: 'Courier New', Courier, monospace;">********</span>
                    </c:when>
                    <c:otherwise>
                    API key: <span class="smaller">(not required)</span>
                    </c:otherwise>
                    </c:choose>
                    <br /><br />
                </p>

                <c:choose>
                <c:when test="${apiKeyRequired}">

                <p>
                    Structurizr parameters for <a href="https://docs.structurizr.com/push" target="_blank">push</a> and <a href="https://docs.structurizr.com/push" target="_blank">pull</a> via the workspace API:
                    <br />
                    <pre id="workspace${workspace.id}Push" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">push -url <span class="baseUrl"></span>/api -id ${workspace.id} -key KEY</pre>
                    <pre id="workspace${workspace.id}Pull" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">pull -url <span class="baseUrl"></span>/api -id ${workspace.id} -key KEY</pre>
                </p>

                <p>
                    Or when using <code>curl</code>:
                    <br />
                    <pre id="workspace${workspace.id}Curl" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">curl <span class="baseUrl"></span>/api/workspace/${workspace.id} --header 'X-Authorization: KEY'</pre>
                </p>

                <c:if test="${showAdminFeatures}">
                <br />
                <form id="regenerateApiCredentialsForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/settings/regenerate-api-credentials" method="post">
                    <input type="hidden" name="workspaceId" value="${workspace.id}" />
                    <button class="btn btn-danger" type="submit" title="Regenerate API credentials"><img src="/static/bootstrap-icons/key.svg" class="icon-btn icon-white" /> Regenerate API credentials</button>
                </form>
                </c:if>

                </c:when>
                <c:otherwise>

                <p>
                    Structurizr parameters for <a href="https://docs.structurizr.com/push" target="_blank">push</a> and <a href="https://docs.structurizr.com/push" target="_blank">pull</a> via the workspace API:
                    <br />
                    <pre id="workspace${workspace.id}Push" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">push -url <span class="baseUrl"></span>/api -id ${workspace.id}</pre>
                    <pre id="workspace${workspace.id}Pull" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">pull -url <span class="baseUrl"></span>/api -id ${workspace.id}</pre>
                </p>

                <p>
                    Or when using <code>curl</code>:
                    <br />
                    <pre id="workspace${workspace.id}Curl" style="font-family: 'Courier New', Courier, monospace; cursor: pointer; text-align: left">curl <span class="baseUrl"></span>/api/workspace/${workspace.id}</pre>
                </p>

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

        <c:if test="${showAdminFeatures && structurizrConfiguration.authenticationEnabled}">
        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Workspace visibility</h4>
            </div>
            <div class="col-10">
                <p>
                    <c:choose>
                    <c:when test="${workspace.shareable}">
                    Sharing link: <a href="/share/${workspace.id}/${workspace.sharingToken}" target="_blank">/share/${workspace.id}/${workspace.sharingToken}</a>
                    </c:when>
                    <c:otherwise>
                    Sharing link: -
                    </c:otherwise>
                    </c:choose>
                    <br />
                    <c:choose>
                    <c:when test="${workspace.publicWorkspace}">
                    Public link: <a href="/share/${workspace.id}" target="_blank">/share/${workspace.id}</a>
                    </c:when>
                    <c:otherwise>
                    Public link: -
                    </c:otherwise>
                    </c:choose>
                </p>

                <br />
                <c:choose>
                    <c:when test="${workspace.publicWorkspace}">
                        <form id="privateWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/settings/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="private" />
                            <button class="btn btn-primary" type="submit" title="Disable public link"><img src="/static/bootstrap-icons/lock.svg" class="icon-btn icon-white" /> Disable public link</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form id="publicWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/settings/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="public" />
                            <button class="btn btn-danger" type="submit" title="Enable public link"><img src="/static/bootstrap-icons/unlock.svg" class="icon-btn icon-white" /> Enable public link</button>
                        </form>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty workspace.sharingToken}">
                        <form id="unshareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/settings/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="unshare" />
                            <button class="btn btn-primary" type="submit" title="Disable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn icon-white" /> Disable sharing link</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form id="shareWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/settings/visibility" method="post">
                            <input type="hidden" name="workspaceId" value="${workspace.id}" />
                            <input type="hidden" name="action" value="share" />
                            <button class="btn btn-primary" type="submit" title="Enable sharing link"><img src="/static/bootstrap-icons/link.svg" class="icon-btn icon-white" /> Enable sharing link</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <hr />
        </c:if>

        <c:if test="${showAdminFeatures}">
        <c:if test="${workspace.locked eq true}">
        <div class="row">
            <div class="col-2">
                <h4 style="margin-top: 0">Unlock workspace</h4>
            </div>
            <div class="col-10">
                <p>
                    Click the button below to unlock your workspace.
                </p>
                <form id="unlockWorkspaceForm" class="form-inline small centered" style="display: inline-block; margin-bottom: 5px" action="/workspace/${workspace.id}/unlock" method="post">
                    <button class="btn btn-warning small" type="submit" name="action" value="remove" title="Unlock workspace"><img src="/static/bootstrap-icons/unlock.svg" class="icon-btn" /> Unlock workspace</button>
                </form>
            </div>
        </div>
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
                    <button class="btn btn-danger small" type="submit" name="action" value="remove" title="Delete workspace"><img src="/static/bootstrap-icons/folder-x.svg" class="icon-white icon-btn" /> Delete workspace</button>
                </form>
            </div>
        </div>
        </c:if>

    </div>
</div>

<script nonce="${scriptNonce}">

    $('#workspace${workspace.id}Id').click(function() { structurizr.util.selectText('workspace${workspace.id}Id'); });
    $('#workspace${workspace.id}ApiUrl').click(function() { structurizr.util.selectText('workspace${workspace.id}ApiUrl'); });
    $('#workspace${workspace.id}Push').click(function() { structurizr.util.selectText('workspace${workspace.id}Push'); });
    $('#workspace${workspace.id}Pull').click(function() { structurizr.util.selectText('workspace${workspace.id}Pull'); });
    $('#workspace${workspace.id}Curl').click(function() { structurizr.util.selectText('workspace${workspace.id}Curl'); });

    $('#regenerateApiCredentialsForm').on('submit', function() { return confirm('Are you sure you want to regenerate the API credentials?'); });

    $('#addClientSideEncryptionButton').click(function() { addClientSideEncryption(); });
    $('#removeClientSideEncryptionButton').click(function() { removeClientSideEncryption(); });
    $('#manageUsersButton').click(function() { window.location.href = '/workspace/${workspace.id}/users'; });

    $('#publicWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to make this workspace public?'); });
    $('#privateWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to make this workspace private?'); });
    $('#shareWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to share this workspace with a link?'); });
    $('#unshareWorkspaceForm').on('submit', function() { return confirm('Are you sure you want to stop sharing this workspace with a link?'); });

    $('#unlockWorkspaceForm').on('submit', function() { return unlockWorkspace(); });
    $('#deleteWorkspaceForm').on('submit', function() { return deleteWorkspace(); });

    function workspaceLoaded() {
        $('.baseUrl').text(window.location.protocol + '//' + window.location.host);
    }

    function unlockWorkspace(e) {
        return confirm('${workspace.lockedUser} will lose any unsaved changes - are you sure you want to unlock this workspace?');
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
            removeWorkspaceEncryptionPassphraseFromLocalStorage(structurizr.workspace.id);
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