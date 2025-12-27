<c:set var="usernameRemembered" value="${not empty username}" />

<div class="section">
    <div class="container">

        <c:if test="${not empty param['error']}">
            <div class="alert alert-danger small" role="alert">Sorry, that username/password combination wasn't recognised - please try again.</div>
        </c:if>

        <div class="row">
            <div class="col-sm-4"></div>
            <div class="col-sm-4 centered">
                <form class="form-horizontal" action="/signin" method="post" role="form">
                    <h1 class="centered">Sign in</h1>
                    <br />

                    <input type="text" class="form-control" id="username" name="username" value="${username}" placeholder="Username" required="true" autocomplete="on" <c:if test="${not usernameRemembered}">autofocus="true"</c:if> />
                    <input id="password" class="form-control" style="margin-top: 10px;" id="password" name="password" placeholder="Password" type="password" required="true" autocomplete="off" <c:if test="${usernameRemembered}">autofocus="true"</c:if> />

                    <div class="centered" style="margin-top: 10px;">
                        <span id="showPassword" class="label structurizrBackgroundLighter" style="cursor:default;"><img src="/static/bootstrap-icons/eye.svg" class="icon-sm icon-white" /> Show password</span>
                        <span id="hidePassword" class="label structurizrBackgroundLighter hidden" style="cursor:default;"><img src="/static/bootstrap-icons/eye-slash.svg" class="icon-sm icon-white" /> Hide password</span>
                    </div>

                    <div class="centered" style="margin-top: 10px;">
                        <c:if test="${pageContext.request.secure}">
                        <input type="checkbox" id="rememberUsername" name="rememberUsername" value="true" <c:if test="${usernameRemembered}">checked="checked"</c:if> />
                        <label for="rememberUsername" class="small">Remember e-mail address</label>
                        <br />
                        <input type="checkbox" id="rememberMe" name="remember-me" value="true" />
                        <label for="rememberMe" class="small">Stay signed in</label>
                        </c:if>

                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        <input type="hidden" id="hash" name="hash" value="" />
                    </div>

                    <div class="centered" style="margin-top: 30px;">
                        <button id="submit" type="submit" class="btn btn-default">Sign In</button>
                    </div>
                </form>
                <div class="col-sm-4"></div>
            </div>
        </div>
    </div>
</div>


<script nonce="${scriptNonce}">

    $('#showPassword').click(function() { showPassword(); });
    $('#hidePassword').click(function() { hidePassword(); });

    if (window.location.hash) {
        $('#hash').val(window.location.hash);
    }

    function showPassword() {
        $('#password').prop('type', 'text');
        $('#showPassword').addClass('hidden');
        $('#hidePassword').removeClass('hidden');
        $('#password').focus();
    }

    function hidePassword() {
        $('#password').prop('type', 'password');
        $('#hidePassword').addClass('hidden');
        $('#showPassword').removeClass('hidden');
        $('#password').focus();
    }
</script>