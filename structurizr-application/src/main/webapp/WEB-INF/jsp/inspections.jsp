<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<style>
    .violationError {
        border-top: #EA2805 solid 1px;
        border-right: #EA2805 solid 1px;
        border-bottom: #EA2805 solid 1px;
        border-left: #EA2805 solid 20px;
    }

    .violationWarning {
        border-top: #EA8205 solid 1px;
        border-right: #EA8205 solid 1px;
        border-bottom: #EA8205 solid 1px;
        border-left: #EA8205 solid 20px;
    }

    .violationInfo {
        border-top: #058CEA solid 1px;
        border-right: #058CEA solid 1px;
        border-bottom: #058CEA solid 1px;
        border-left: #058CEA solid 20px;
    }

    .violationIgnore {
        border-top: #dddddd solid 1px;
        border-right: #dddddd solid 1px;
        border-bottom: #dddddd solid 1px;
        border-left: #dddddd solid 20px;
    }

    .table>tbody>tr>td {
        border: none;
        padding: 20px;
    }
</style>

<div class="section">
    <div class="container centered">
        <h1><c:out value="${workspace.name}" escapeXml="true" /></h1>
        <p>
            <c:out value="${workspace.description}" escapeXml="true" />
        </p>

        <br />

        <p style="font-size: 25px; margin-top: 20px">
            <img src="/static/bootstrap-icons/exclamation-diamond.svg" class="icon-sm" /> Error: <span>${numberOfErrors}</span> |
            <img src="/static/bootstrap-icons/exclamation-triangle.svg" class="icon-sm" /> Warning: <span>${numberOfWarnings}</span> |
            <img src="/static/bootstrap-icons/info-circle.svg" class="icon-sm" /> Info: <span>${numberOfInfos}</span> |
            <img src="/static/bootstrap-icons/x-circle.svg" class="icon-sm" /> Ignore: <span>${numberOfIgnores}</span>
        </p>
        <p style="margin-top: 5px">
            <img src="/static/bootstrap-icons/clipboard-pulse.svg" class="icon-sm" /> Inspections: <span>${numberOfInspections}</span> |
            <a href="https://docs.structurizr.com/workspaces/inspections" target="_blank">Help</a>
        </p>

        <br />

        <c:if test="${not empty error}">
        <c:out value="${error}" />
        </c:if>

        <div style="text-align: left; font-size: 16px">
            <table class="table">
                <tbody>
                <c:forEach var="violation" items="${violations}" varStatus="status">
                    <c:choose>
                    <c:when test="${violation.severity eq 'ERROR'}">
                    <tr class="violationError">
                    <td style="width: 150px"><img src="/static/bootstrap-icons/exclamation-diamond.svg" class="icon-sm" /> ERROR</td>
                    </c:when>
                    <c:when test="${violation.severity eq 'WARNING'}">
                    <tr class="violationWarning">
                    <td style="width: 150px"><img src="/static/bootstrap-icons/exclamation-triangle.svg" class="icon-sm" /> WARNING</td>
                    </c:when>
                    <c:when test="${violation.severity eq 'INFO'}">
                    <tr class="violationInfo">
                    <td style="width: 150px"><img src="/static/bootstrap-icons/info-circle.svg" class="icon-sm" /> INFO</td>
                    </c:when>
                    <c:otherwise>
                    <tr class="violationIgnore">
                    <td style="width: 150px"><img src="/static/bootstrap-icons/x-circle.svg" class="icon-sm" /> IGNORE</td>
                    </c:otherwise>
                    </c:choose>
                        <td>
                            ${status.count}. <c:out value="${violation.message}" />
                            <div class="smaller" style="margin-top: 10px"><c:out value="${violation.type}" /></div>
                        </td>
                    </tr>
                    <tr style="height: 10px"><td colspan="3"></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    function workspaceLoaded() {
        structurizr.ui.applyWorkspaceLogo();
    }
</script>

<%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>

<c:if test="${structurizrConfiguration.profile eq 'Local'}">
    <%@ include file="/WEB-INF/fragments/workspace/auto-refresh.jspf" %>
</c:if>