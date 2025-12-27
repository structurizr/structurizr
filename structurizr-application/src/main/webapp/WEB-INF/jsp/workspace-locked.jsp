<div class="section">
    <div class="container">
        <h1>Workspace locked</h1>
        <p>
            This workspace was locked by <code>${workspace.lockedUser}</code> at <fmt:formatDate value="${workspace.lockedDate}" pattern="EEE dd MMM yyyy HH:mm z" timeZone="${user.timeZone}" />.
        </p>

        <p class="small">
            <a href="/workspace/${workspace.id}${urlSuffix}">Workspace summary</a>
            |
            <a href="/workspace/${workspace.id}/diagrams${urlSuffix}">Diagram viewer</a>
        </p>
    </div>
</div>