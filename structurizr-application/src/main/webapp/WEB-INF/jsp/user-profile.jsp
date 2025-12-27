<div class="section">
    <div class="container">
        <h1>User profile</h1>

        <p>
            Username: ${user.username}
        </p>

        <p>
            Roles:
        </p>

        <ul>
        <c:forEach var="role" items="${user.roles}">
            <li>${role}</li>
        </c:forEach>
        </ul>
    </div>
</div>