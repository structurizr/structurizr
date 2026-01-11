<div class="section">
    <div class="container">
        <h1>${user.username}</h1>

        <br />
        <p>
            Roles:
        </p>

        <ul>
        <c:forEach var="role" items="${user.roles}">
            <li>${role}</li>
        </c:forEach>
        </ul>

        <c:if test="${adminUsersEnabled}">
        <p>
            <c:choose>
            <c:when test="${user.admin}">
            Admin user: Yes
            </c:when>
            <c:otherwise>
            Admin user: No
            </c:otherwise>
            </c:choose>
        </p>
        </c:if>

        <p class="centered">
            <a href="/signout"><span class="label structurizrBackgroundLight" style="font-size: 18px"><img src="/static/bootstrap-icons/box-arrow-right.svg" class="icon-sm icon-white" /> Sign out</span></a>
        </p>
    </div>
</div>