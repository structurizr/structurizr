<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<script type="text/javascript" src="<c:url value="/static/js/structurizr-lock.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/structurizr-embed.js" />"></script>
<script type="text/javascript" src="/static/js/ace-1.5.0.min.js" charset="utf-8"></script>

<%@ include file="/WEB-INF/fragments/tooltip.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/dsl/introduction.jspf" %>

<style>
    .label-branch, .label-version {
        font-size: 10px;
        padding: 4px 8px 4px 8px;
        margin-left: 20px;
    }
</style>

<script nonce="${scriptNonce}">
    progressMessage.show('<p>Loading workspace...</p>');
</script>

<div class="centered">
    <div id="banner"></div>
</div>

<div id="errorMessageAlert" class="alert alert-danger small hidden centered">
    <span id="errorMessage"></span>
</div>

<div class="section" style="padding-top: 20px; padding-bottom: 0">
    <div class="row" style="margin-left: 0; margin-right: 0; padding-bottom: 0">
        <div id="sourcePanel" class="col-6 centered">

            <div style="text-align: left; margin-bottom: 10px">
                <div id="sourceControls" style="float: right">
                    <div class="btn-group">
                        <button id="dashboardButton" class="btn btn-default" title="Return to dashboard"><img src="/static/bootstrap-icons/house.svg" class="icon-btn" /></button>
                        <button id="workspaceSummaryButton" class="btn btn-default" title="Workspace summary"><img src="/static/bootstrap-icons/folder.svg" class="icon-btn" /></button>
                    </div>
                    <label class="btn btn-default small">
                        <img src="/static/bootstrap-icons/cloud-upload.svg" title="Upload DSL" class="icon-btn" />
                        <input id="uploadFileInput" type="file" style="display: none;">
                    </label>
                    <div class="btn-group">
                        <button id="sourceButton" class="btn btn-default" title="Source"><img src="/static/bootstrap-icons/code-slash.svg" class="icon-btn" /></button>
                        <button id="diagramsButton" class="btn btn-default" title="Diagrams"><img src="/static/bootstrap-icons/bounding-box.svg" class="icon-btn" /></button>
                    </div>
                    <button id="saveButton" class="btn btn-default" title="Save workspace" disabled="disabled" style="text-shadow: none"><img src="/static/bootstrap-icons/folder-check.svg" class="icon-btn" /></button>
                    <button id="renderButton" class="btn btn-primary"><img src="/static/bootstrap-icons/play.svg" class="icon-btn icon-white" /></button>
                </div>

                <div>
                <%@ include file="/WEB-INF/fragments/dsl/language-reference.jspf" %>
                </div>
            </div>

            <div id="sourceTextArea"></div>

            <div class="smaller" style="margin-top: 5px">
                <a id="renderingModeLightLink" href="" title="Light"><img src="/static/bootstrap-icons/sun.svg" class="icon-xs" /></a> |
                <a id="renderingModeDarkLink" href="" title="Dark"><img src="/static/bootstrap-icons/moon-fill.svg" class="icon-xs" /></a> |
                <a id="renderingModeSystemLink" href="" title="System"><img src="/static/bootstrap-icons/sliders.svg" class="icon-xs" /></a>

                <c:if test="${not empty workspace.branch}">
                    <span class="label label-branch"><img src="/static/bootstrap-icons/bezier2.svg" class="icon-xs icon-white" /> ${workspace.branch}</span>
                </c:if>

                <c:if test="${not empty param.version}">
                    <span class="label label-version"><img src="/static/bootstrap-icons/clock-history.svg" class="icon-xs icon-white" /> ${workspace.userFriendlyInternalVersion}</span>
                </c:if>
            </div>
        </div>
        <div id="diagramsPanel" class="col-6 centered">
            <div id="viewListPanel" style="margin-bottom: 10px">
                <div class="form-inline">
                        <span id="diagramNavButtons" class="hidden">
                            <button id="viewSourceButton" class="btn btn-primary" title="Source" style="margin-top: -4px;"><img src="/static/bootstrap-icons/code-slash.svg" class="icon-btn icon-white" /> View source</button>
                        </span>
                    <select id="viewsList" class="form-select" style="width: auto; display: inline-block;"></select>
                </div>
            </div>

            <div>
                <div id="diagramEditor"></div>
            </div>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    var viewInFocus;
    var editor;
    var editorRendered = false;
    var structurizrDiagramIframeRendered = false;
    var sourceVisible = true;
    var diagramsVisible = true;
    var unsavedChanges = false;

    $('#dashboardButton').click(function() { window.location.href='/'; });
    $('#workspaceSummaryButton').click(function() { window.location.href='${urlPrefix}${urlSuffix}'; });
    $('#sourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#diagramsButton').click(function(event) { diagramsButtonClicked(event); });
    $('#viewSourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#saveButton').click(function() { saveWorkspace(); });
    $('#renderButton').click(function() { refresh(); });

    $('#uploadFileInput').on('change', function() { importSourceFile(this.files); });

    function reloadWorkspace() {
        structurizrDiagramIframeRendered = false;
        hideError();
        showSourceAndDiagrams();

        structurizrApiClient.getWorkspace(undefined,
            function(response) {
                const json = response.json;
                json.id = ${workspace.id};
                if (json.ciphertext && json.encryptionStrategy) {
                    showPassphraseModalAndDecryptWorkspace(json, loadWorkspace);
                } else {
                    loadWorkspace(json);
                }
            }
        );
    }

    window.onresize = resize;

    $(window).on("beforeunload", function() {
        return beforeunload();
    });

    $(window).on("unload", function() {
        navigator.sendBeacon('/workspace/${workspace.id}/unlock?agent=${userAgent}');
    });

    function workspaceLoaded() {
        init();
    }

    function init() {
        renderEditor();
        renderDiagrams();

        if (!structurizr.workspace.hasElements() && !structurizr.workspace.hasViews() && !structurizr.workspace.hasDocumentation() && !structurizr.workspace.hasDecisions()) {
            $('#dslEditorIntroductionModal').modal('show');
        }
    }

    var hasUnparsedDSL = false;
    var applyingRemoteChanges = false;

    function renderEditor() {
        if (editorRendered === false) {
            editor = ace.edit("sourceTextArea");
            editor.session.setOptions({
                tabSize: 4,
                useSoftTabs: true
            });
            ace.config.set('basePath', '/static/js/ace');
            editor.session.setMode("ace/mode/structurizr");
            editor.setOption("printMargin", false);

            var editorSource;
            var dslSource = structurizr.workspace.getProperty('structurizr.dsl');
            if (dslSource !== undefined) {
                editorSource = structurizr.util.atob(dslSource);
            } else {
                editorSource = 'workspace "Name" "Description" {\n\n\tmodel {\n\t}\n\n\tconfiguration {\n\t\tscope softwaresystem\n\t}\n\n}';
            }

            editorSource = editorSource.replaceAll('\t', '    ');
            editor.setValue(editorSource, -1);

            editor.session.getUndoManager().markClean();
            editor.session.on('change', function(delta) {
                hasUnparsedDSL = true;
            });

            editorRendered = true;
        }
    }

    function renderDiagrams() {
        var viewsList = $('#viewsList');
        viewsList.empty();

        viewInFocus = structurizr.workspace.views.configuration.lastSavedView;

        var listOfViews = structurizr.workspace.getViews();
        if (listOfViews.length > 0) {
            for (var i = 0; i < listOfViews.length; i++) {
                const view = listOfViews[i];
                viewsList.append('<option value="' + structurizr.util.escapeHtml(view.key) + '">' + structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view)) + ' (#' + view.key + ')</option>');
            }

            if (viewInFocus === undefined || viewInFocus === '') {
                viewInFocus = listOfViews[0].key;
            }

            if (structurizr.workspace.findViewByKey(viewInFocus) === undefined) {
                viewInFocus = listOfViews[0].key;
            }

            viewsList.val(viewInFocus);

            viewsList.change(function () {
                viewInFocus = $(this).val();
                console.log(viewInFocus);
                changeView();
            });
        }

        resize();

        if (structurizr.workspace.hasViews()) {
            renderStructurizrDiagram();
        } else {
            hideStructurizrDiagram();
        }

        progressMessage.hide();
    }

    function resize() {
        const sourceControlsHeight = $('#sourceControls').outerHeight();
        const bannerHeight = $('#banner').outerHeight();
        const verticalPadding = 60;

        $('#sourceTextArea').css('height', (window.innerHeight - sourceControlsHeight - bannerHeight - verticalPadding) + 'px');
        if (editor) {
            editor.resize(true);
        }
        structurizr.embed.setMaxHeight(window.innerHeight - bannerHeight - verticalPadding - 20);
        structurizr.embed.resizeEmbeddedDiagrams();
    }

    function renderStructurizrDiagram() {
        if (structurizrDiagramIframeRendered === false) {
            var diagramEditorDiv = $('#diagramEditor');
            diagramEditorDiv.empty();

            var diagramIdentifier = viewInFocus;
            var domId = 'diagramEditorIframe';
            var embedUrl = '/embed?workspace=${workspace.id}&branch=${workspace.branch}&version=${param.version}&view=' + encodeURIComponent(diagramIdentifier) + '&editable=true&urlPrefix=${urlPrefix}&iframe=' + domId;
            diagramEditorDiv.append('<div style="text-align: center"><iframe id="' + domId + '" class="structurizrEmbed thumbnail" src="' + embedUrl + '" width="100%" height="' + window.innerHeight + 'px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowfullscreen="true"></iframe></div>');

            setTimeout(function () {
                try {
                    document.getElementById('diagramEditorIframe').contentWindow.structurizr.scripting = undefined;
                    document.getElementById('diagramEditorIframe').contentWindow.structurizr.diagram.onWorkspaceChanged(workspaceChanged);
                    document.getElementById('diagramEditorIframe').contentWindow.structurizr.diagram.onViewChanged(function(view) {
                        document.getElementById('diagramEditorIframe').contentWindow.viewChanged(view);

                        if (document.getElementById('viewsList').value !== view) {
                            document.getElementById('viewsList').value = view;
                        }
                    });
                } catch (e) {
                }
            }, 2000);

            structurizrDiagramIframeRendered = true;
        } else {
            changeView();
        }
    }

    function hideStructurizrDiagram() {
        structurizrDiagramIframeRendered = false;
        $('#diagramEditor').empty();
    }

    function changeView() {
        if (structurizr.workspace.hasViews()) {
            document.getElementById('diagramEditorIframe').contentWindow.changeView(structurizr.workspace.findViewByKey(viewInFocus));
            $('#diagramEditorIframe').focus();
        }
    }

    function workspaceChanged() {
        $('#saveButton').removeClass('btn-default');
        $('#saveButton').addClass('btn-danger');
        $('#saveButton img').addClass('icon-white');
        $('#saveButton').prop('disabled', false);
        unsavedChanges = true;
    }

    function beforeunload() {
        if (unsavedChanges || !editor.session.getUndoManager().isClean()) {
            return "There are unsaved changes.";
        }
    }

    function refresh() {
        progressMessage.show('<p>Loading workspace...</p>');

        hasUnparsedDSL = false;

        structurizr.workspace.views.configuration.lastSavedView = viewInFocus;
        const workspace = structurizr.workspace.getJson();

        if (workspace.properties === undefined) {
            workspace.properties = {};
        }
        workspace.properties['structurizr.dsl'] = structurizr.util.btoa(editor.getValue());
        workspace.views = structurizr.workspace.views;

        const jsonAsString = JSON.stringify(workspace);

        $.ajax({
            url: '/workspace/${workspace.id}/dsl-editor',
            type: "POST",
            contentType: 'application/json; charset=UTF-8',
            cache: false,
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            dataType: 'json',
            data: jsonAsString
        })
        .done(function(data, textStatus, jqXHR) {
            if (data.success === true) {
                structurizrDiagramIframeRendered = false;
                hideError();
                showSourceAndDiagrams();
                loadWorkspace(JSON.parse(data.workspace));
                workspaceChanged();
            } else {
                showError(data.message);
                if (data.lineNumber > 0) {
                    const line = data.lineNumber -1;
                    editor.moveCursorToPosition({row: line, column: 0});
                    editor.selection.selectLine();
                    editor.scrollToLine(line);
                }
                progressMessage.hide();
            }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(jqXHR.status);
            console.log("Text status: " + textStatus);
            console.log("Error thrown: " + errorThrown);
        });
    }

    function hideError() {
        $('#errorMessageAlert').addClass('hidden');
    }

    function showError(message) {
        $('#errorMessageAlert').removeClass('hidden');
        $('#errorMessage').text(message);
    }

    function sourceButtonClicked(e) {
        e.preventDefault();
        if (sourceVisible === false || diagramsVisible === false) {
            showSourceAndDiagrams();
        } else {
            hideDiagrams();
        }

        editor.focus();
    }

    function diagramsButtonClicked(e) {
        e.preventDefault();
        if (diagramsVisible === false || sourceVisible === false) {
            showSourceAndDiagrams();
        } else {
            hideSource();
        }

        $('#diagramEditorIframe').focus();
    }

    function hideSource() {
        $('#sourcePanel').addClass('hidden');
        $('#diagramsPanel').removeClass('col-6');

        sourceVisible = false;
        $('#diagramNavButtons').removeClass('hidden');
        resize();
    }

    function showSourceAndDiagrams() {
        $('#sourcePanel').removeClass('hidden');
        $('#sourcePanel').addClass('col-6');
        $('#diagramsPanel').removeClass('hidden');
        $('#diagramsPanel').addClass('col-6');

        $('#diagramNavButtons').addClass('hidden');

        sourceVisible = true;
        diagramsVisible = true;

        resize();
    }

    function hideDiagrams() {
        $('#diagramsPanel').addClass('hidden');
        $('#sourcePanel').removeClass('col-6');

        diagramsVisible = false;
        $('#sourceButton').addClass('hidden');
        $('#diagramsButton').removeClass('hidden');
        resize();
    }

    function saveWorkspace() {
        var save = true;
        $('#saveButton').prop('disabled', true);

        if (hasUnparsedDSL === true) {
            save = confirm("Warning: you have changes in the DSL editor that have not been rendered yet (these changes will not be included in your workspace).");
        }

        if (save) {
            try {
                const embeddedDiagramEditor = document.getElementById('diagramEditorIframe');
                if (embeddedDiagramEditor && embeddedDiagramEditor.contentWindow && structurizr.workspace.hasViews()) {
                    structurizr.workspace.views.configuration.lastSavedView = embeddedDiagramEditor.contentWindow.structurizr.diagram.getCurrentViewOrFilter().key;
                }
            } catch (err) {
                console.log(err);
            }

            structurizr.saveWorkspace(function(response) {
                if (response.success === true) {
                    progressMessage.hide();

                    $('#saveButton').removeClass('btn-danger');
                    $('#saveButton').addClass('btn-default');
                    $('#saveButton img').removeClass('icon-white');
                    $('#saveButton').prop('disabled', true);

                    unsavedChanges = false;
                    editor.session.getUndoManager().markClean();

                    try {
                        const embeddedDiagramEditor = document.getElementById('diagramEditorIframe');
                        if (embeddedDiagramEditor && embeddedDiagramEditor.contentWindow && structurizr.workspace.hasViews()) {
                            embeddedDiagramEditor.contentWindow.refreshThumbnail();
                        }
                    } catch (err) {
                        console.log(err);
                    }
                } else {
                    $('#saveButton').prop('disabled', false);
                    if (response.message) {
                        console.log(response.message);
                        if (progressMessage) {
                            progressMessage.show(response.message);
                        }
                    }
                }
            });
        }
    }

    function importSourceFile(files) {
        if (files && files.length > 0) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                var content = evt.target.result;
                editor.setValue(content, -1);
            };

            reader.readAsText(files[0]);
        }
    }

    <c:if test="${workspace.editable && not empty workspace.apiKey}">
    new structurizr.Lock(${workspace.id}, '${userAgent}');
    </c:if>
</script>

<c:choose>
    <c:when test="${not empty workspaceAsJson}">
        <%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
        <%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>