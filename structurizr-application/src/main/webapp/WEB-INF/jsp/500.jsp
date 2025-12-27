<div class="section">
    <div class="container">
        <h1>Error</h1>
        <p>
        <c:choose>
            <c:when test="${not empty customErrorMessage}">
                ${customErrorMessage}
            </c:when>
            <c:otherwise>
                Sorry, something went wrong.
            </c:otherwise>
        </c:choose>
        </p>
    </div>
</div>