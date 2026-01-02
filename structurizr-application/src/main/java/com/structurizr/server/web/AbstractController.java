package com.structurizr.server.web;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.security.SecurityUtils;
import com.structurizr.util.RandomGuidGenerator;
import com.structurizr.util.StringUtils;
import com.structurizr.util.Version;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.TimeZone;

public abstract class AbstractController {

    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    private static final String REFERER_POLICY_HEADER = "Referrer-Policy";
    private static final String REFERER_POLICY_VALUE = "strict-origin-when-cross-origin";
    private static final String SCRIPT_NONCE_ATTRIBUTE = "scriptNonce";

    protected static final String URL_PREFIX = "urlPrefix";
    private static final String URL_SUFFIX = "urlSuffix";

    private static final Log log = LogFactory.getLog(AbstractController.class);
    private static final String STRUCTURIZR_CSS_FILENAME = "structurizr.css";
    private static final String STRUCTURIZR_JS_FILENAME = "structurizr.js";

    @ModelAttribute("structurizrConfiguration")
    public Configuration getConfiguration() {
        return Configuration.getInstance();
    }

    @ModelAttribute
    protected void addSecurityHeaders(HttpServletResponse response, ModelMap model) {
        response.addHeader(REFERER_POLICY_HEADER, REFERER_POLICY_VALUE);

        String nonce = Base64.getEncoder().encodeToString(new RandomGuidGenerator().generate().getBytes(StandardCharsets.UTF_8));
        model.addAttribute(SCRIPT_NONCE_ATTRIBUTE, nonce);

        response.addHeader(CONTENT_SECURITY_POLICY_HEADER, String.format("script-src 'self' 'nonce-%s'; worker-src blob:", nonce));
    }

    protected void addCommonAttributes(ModelMap model, boolean showHeaderAndFooter) {
        addCommonAttributes(model, "", showHeaderAndFooter);
    }

    protected void addCommonAttributes(ModelMap model, String pageTitle, boolean showHeaderAndFooter) {
        model.addAttribute("timeZone", TimeZone.getDefault().getID());
        if (model.getAttribute("showHeader") == null) {
            model.addAttribute("showHeader", showHeaderAndFooter);
        }
        if (model.getAttribute("showFooter") == null) {
            model.addAttribute("showFooter", showHeaderAndFooter);
        }
        model.addAttribute("version", new Version());

        model.addAttribute("authenticated", isAuthenticated());
        User user = getUser();
        model.addAttribute("user", user);
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }

        model.addAttribute("authenticationEnabled", Configuration.getInstance().isAuthenticationEnabled());
        model.addAttribute("searchEnabled", Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_SEARCH));
        model.addAttribute("dslEditorEnabled", Configuration.getInstance().isFeatureEnabled(Features.UI_DSL_EDITOR));

        File cssFile = new File(Configuration.getInstance().getDataDirectory(), STRUCTURIZR_CSS_FILENAME);
        if (cssFile.exists()) {
            try {
                model.addAttribute("css", Files.readString(cssFile.toPath()));
            } catch (IOException ioe) {
                log.warn(ioe);
            }
        }

        File jsFile = new File(Configuration.getInstance().getDataDirectory(), STRUCTURIZR_JS_FILENAME);
        if (jsFile.exists()) {
            try {
                model.addAttribute("js", Files.readString(jsFile.toPath()));
            } catch (IOException ioe) {
                log.warn(ioe);
            }
        }

        if (StringUtils.isNullOrEmpty(pageTitle)) {
            model.addAttribute("pageTitle", "Structurizr");
        } else {
            model.addAttribute("pageTitle", "Structurizr - " + pageTitle);
        }

        model.addAttribute("diagramReviewFeatureEnabled", Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS));
    }

    protected String calculateUrlPrefix(long workspaceId) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return "/workspace";
        } else {
            return "/workspace/" + workspaceId;
        }
    }

    protected String show404Page(ModelMap model) {
        addCommonAttributes(model, "Not found", true);

        return "404";
    }

    protected String show500Page(ModelMap model) {
        addCommonAttributes(model, "Error", true);

        return "500";
    }

    protected String showError(String view, ModelMap model) {
        addCommonAttributes(model, "", true);

        return view;
    }

    protected final User getUser() {
        return SecurityUtils.getUser();
    }

    protected final boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        } else {
            return authentication.isAuthenticated();
        }
    }

    protected final void addUrlSuffix(String branch, String version, ModelMap model) {
        if (!StringUtils.isNullOrEmpty(branch) && !StringUtils.isNullOrEmpty(version)) {
            WorkspaceBranch.validateBranchName(branch);
            WorkspaceVersion.validateVersionIdentifier(version);
            model.addAttribute(URL_SUFFIX, String.format("?branch=%s&version=%s", branch, version));

        } else if (!StringUtils.isNullOrEmpty(branch)) {
            WorkspaceBranch.validateBranchName(branch);
            model.addAttribute(URL_SUFFIX, String.format("?branch=%s", branch));

        } else if (!StringUtils.isNullOrEmpty(version)) {
            WorkspaceVersion.validateVersionIdentifier(version);
            model.addAttribute(URL_SUFFIX, String.format("?version=%s", version));
        }
    }

    protected String showFeatureNotAvailablePage(ModelMap model) {
        addCommonAttributes(model, "Feature not available", true);

        return "feature-not-available";
    }

}