<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<script type="text/javascript" src="<c:url value="/static/js/structurizr-embed.js" />"></script>
<script type="text/javascript" src="/static/js/ace-1.5.0.min.js" charset="utf-8"></script>

<%@ include file="/WEB-INF/fragments/tooltip.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<script nonce="${scriptNonce}">
    progressMessage.show('<p>Loading workspace...</p>');
</script>

<div class="section" style="padding-top: 20px; padding-bottom: 0">
    <div class="row" style="margin-left: 0; margin-right: 0; padding-bottom: 0">
        <div id="sourcePanel" class="col-6 centered">

            <form id="dslForm" action="/" method="post">
                <input id="dsl" name="dsl" type="hidden" value="" />
                <input id="view" name="view" type="hidden" />
                <input id="json" name="json" type="hidden" />

                <div style="text-align: left; margin-bottom: 10px">
                    <div id="sourceControls" style="float: right">
                        <div class="btn-group">
                            <label class="btn btn-default small">
                                <img src="/static/bootstrap-icons/file-earmark-arrow-up.svg" class="icon-btn" />
                                <input id="uploadFileInput" type="file" style="display: none;">
                            </label>
                        </div>

                        <div class="btn-group">
                            <button id="loadFromLocalStorageButton" class="btn btn-default" title="Load from local storage"><img src="/static/bootstrap-icons/box-arrow-down.svg" class="icon-btn" /></button>
                            <button id="saveToLocalStorageButton" class="btn btn-default" title="Save to local storage"><img src="/static/bootstrap-icons/box-arrow-up.svg" class="icon-btn" /></button>
                        </div>

                        <div class="btn-group">
                            <button id="sourceButton" class="btn btn-default" title="Source"><img src="/static/bootstrap-icons/code-slash.svg" class="icon-btn" /></button>
                            <button id="diagramsButton" class="btn btn-default" title="Diagrams"><img src="/static/bootstrap-icons/bounding-box.svg" class="icon-btn" /></button>
                        </div>

                        <button id="renderButton" class="btn btn-primary"><img src="/static/bootstrap-icons/play.svg" class="icon-btn icon-white" /></button>
                    </div>

                    <div>
                        <a href="https://docs.structurizr.com" target="_blank"><img src="/static/img/structurizr-logo.png" alt="Structurizr logo" class="img-responsive" style="max-height: 38px; margin-top: -3px;" /></a>
                        <%@ include file="/WEB-INF/fragments/dsl/language-reference.jspf" %>
                    </div>
                </div>

                <div id="sourceTextArea"><c:out value="${workspaceAsDsl}" /></div>
            </form>

            <div class="smaller" style="margin-top: 5px">
                <a href="https://docs.structurizr.com" target="_blank">Structurizr v${version.buildNumber}</a>
                &nbsp;&nbsp;&nbsp;
                <a id="renderingModeLightLink" href="" title="Light"><img src="/static/bootstrap-icons/sun.svg" class="icon-xs" /></a> |
                <a id="renderingModeDarkLink" href="" title="Dark"><img src="/static/bootstrap-icons/moon-fill.svg" class="icon-xs" /></a> |
                <a id="renderingModeSystemLink" href="" title="System"><img src="/static/bootstrap-icons/sliders.svg" class="icon-xs" /></a>
            </div>
        </div>
        <div id="diagramsPanel" class="col-6 centered">

            <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <c:out value="${errorMessage}" escapeXml="true" />
                </div>
            </c:when>
            <c:otherwise>

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

            </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    const LOCAL_STORAGE_DSL = 'structurizr/playground/dsl';
    const LOCAL_STORAGE_JSON = 'structurizr/playground/json';

    const EMBEDDED_DIAGRAM_DOM_ID = 'embeddedStructurizrDiagram';

    var viewInFocus = '<c:out value="${view}" />';
    var editor;
    var structurizrDiagramIframeRendered = false;

    window.onresize = resize;

    $('#homeButton').click(function(event) { event.preventDefault(); window.location.href = '/'; });
    $('#loadFromLocalStorageButton').click(function(event) { loadFromLocalStorage(event); });
    $('#saveToLocalStorageButton').click(function(event) { saveToLocalStorage(event); });
    $('#sourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#diagramsButton').click(function(event) { diagramsButtonClicked(event); });
    $('#viewSourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#renderButton').click(function() { refresh(); });

    $('#uploadFileInput').on('change', function() { importSourceFile(this.files); });

    function workspaceLoaded() {
        init();
    }

    function init() {
        var listOfViews = structurizr.workspace.getViews();
        if (listOfViews.length > 0) {
            const viewsList = $('#viewsList');
            viewsList.empty();

            for (var i = 0; i < listOfViews.length; i++) {
                var view = listOfViews[i];
                viewsList.append('<option value="' + structurizr.util.escapeHtml(view.key) + '">' + structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view)) + ' (#' + view.key + ')</option>');
            }

            if (viewInFocus === undefined || viewInFocus === '') {
                viewInFocus = structurizr.workspace.getViews()[0].key;
            }

            if (structurizr.workspace.findViewByKey(viewInFocus) === undefined) {
                viewInFocus = structurizr.workspace.getViews()[0].key;
            }

            viewsList.val(viewInFocus);

            viewsList.change(function () {
                viewInFocus = $(this).val();
                changeView();
            });
        }

        editor = ace.edit("sourceTextArea");
        editor.setOption("printMargin", false);

        ace.config.set('basePath', '/static/js/ace');
        editor.session.setMode("ace/mode/structurizr");

        resize();

        <c:if test="${line gt 0}">
        var line = ${line}-1;
        editor.moveCursorToPosition({row: line, column: 0});
        editor.selection.selectLine();
        editor.scrollToLine(line);
        </c:if>

        setUnsavedChanges(true);

        $(window).on("beforeunload", function () {
            if (unsavedChanges) {
                return "There are unsaved changes.";
            }
        });

        renderDiagram();

        progressMessage.hide();
    }

    function resize() {
        const sourceControlsHeight = $('#sourceControls').outerHeight();
        const verticalPadding = 60;

        $('#sourceTextArea').css('height', (window.innerHeight - sourceControlsHeight - verticalPadding) + 'px');
        if (editor) {
            editor.resize(true);
        }
        structurizr.embed.setMaxHeight(window.innerHeight - verticalPadding - 20);
        structurizr.embed.resizeEmbeddedDiagrams();
    }

    function renderDiagram() {
        if (structurizrDiagramIframeRendered === false) {
            var diagramEditorDiv = $('#diagramEditor');
            diagramEditorDiv.empty();

            var diagramIdentifier = viewInFocus;
            var embedUrl = '/embed?view=' + encodeURIComponent(diagramIdentifier) + '&editable=true&iframe=' + EMBEDDED_DIAGRAM_DOM_ID;
            diagramEditorDiv.append('<div style="text-align: center"><iframe id="' + EMBEDDED_DIAGRAM_DOM_ID + '" class="structurizrEmbed thumbnail" src="' + embedUrl + '" width="100%" height="' + window.innerHeight + 'px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowfullscreen="true"></iframe></div>');

            structurizrDiagramIframeRendered = true;
            setTimeout(registerCallback, 500);
        } else {
            changeView();
        }
    }

    function registerCallback() {
        const embeddedDiagram = getEmbeddedDiagram();
        if (embeddedDiagram === undefined || embeddedDiagram.contentWindow === undefined || embeddedDiagram.contentWindow.structurizr === undefined || embeddedDiagram.contentWindow.structurizr.diagram === undefined) {
            setTimeout(registerCallback, 500);
        } else {
            try {
                embeddedDiagram.contentWindow.structurizr.scripting = undefined;

                embeddedDiagram.contentWindow.structurizr.diagram.onViewChanged(function (view) {
                    embeddedDiagram.contentWindow.viewChanged(view);

                    if (document.getElementById('viewsList').value !== view) {
                        document.getElementById('viewsList').value = view;
                    }
                });
            } catch (e) {
                console.log(e);
            }
        }
    }

    function changeView() {
        const diagramIdentifier = $('#viewsList').val();
        getEmbeddedDiagram().contentWindow.location.hash = '#' + diagramIdentifier;
    }

    function getEmbeddedDiagram() {
        return document.getElementById(EMBEDDED_DIAGRAM_DOM_ID);
    }

    var unsavedChanges = false;

    function setUnsavedChanges(bool) {
        unsavedChanges = bool;
    }

    function refresh() {
        setUnsavedChanges(false);
        const embeddedDiagram = getEmbeddedDiagram();
        if (embeddedDiagram) {
            embeddedDiagram.contentWindow.unsavedChanges = false;
        }

        const dsl = editor.getValue();
        if (dsl === undefined || dsl.length === 0) {
            $('#dsl').val('');
            $('#json').val('');
        } else {
            $('#dsl').val(dsl);

            $('#view').val($('#viewsList').val());

            structurizr.workspace.views.configuration.lastSavedView = viewInFocus;
            const workspace = structurizr.workspace.getJson();

            if (workspace.properties === undefined) {
                workspace.properties = {};
            }
            workspace.properties['structurizr.dsl'] = structurizr.util.btoa(editor.getValue());
            workspace.views = structurizr.workspace.views;

            const jsonAsString = JSON.stringify(workspace);
            $('#json').val(jsonAsString);
        }

        return true;
    }

    var sourceVisible = true;
    var diagramsVisible = true;

    function loadFromLocalStorage(e) {
        e.preventDefault();

        const dslFromLocalStorage = localStorage.getItem(LOCAL_STORAGE_DSL);
        if (dslFromLocalStorage && dslFromLocalStorage.length > 0) {
            try {
                $('#dsl').val(structurizr.util.atob(dslFromLocalStorage));

                const jsonFromLocalStorage = localStorage.getItem(LOCAL_STORAGE_JSON);
                if (jsonFromLocalStorage && jsonFromLocalStorage.length > 0) {
                    $('#json').val(structurizr.util.atob(jsonFromLocalStorage));
                }

                document.getElementById('dslForm').submit();
            } catch (e) {
                editor.setValue('', -1);
            }
        }
    }

    function saveToLocalStorage(e) {
        e.preventDefault();

        try {
            localStorage.setItem(LOCAL_STORAGE_DSL, structurizr.util.btoa(editor.getValue()));

            if (structurizr.workspace) {
                const workspace = structurizr.workspace.getJson();
                workspace.views = structurizr.workspace.views;

                const jsonAsString = JSON.stringify(workspace);
                localStorage.setItem(LOCAL_STORAGE_JSON, structurizr.util.btoa(jsonAsString));
            }
        } catch (e) {
            console.log(e);
        }
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
        resize();
    }

    function hideSourceAndDiagrams(message) {
        $('#sourcePanel').addClass('hidden');
        $('#diagramsPanel').addClass('hidden');
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

    var searchParams = new URLSearchParams(window.location.search);
    if (searchParams.get('src')) {
        hideSource();
    }
</script>

<%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>