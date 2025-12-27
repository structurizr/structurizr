package com.structurizr.playground;

import com.structurizr.configuration.Configuration;
import com.structurizr.util.RandomGuidGenerator;
import com.structurizr.util.Version;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.TimeZone;

abstract class AbstractController {

    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    private static final String REFERER_POLICY_HEADER = "Referrer-Policy";
    private static final String REFERER_POLICY_VALUE = "strict-origin-when-cross-origin";
    private static final String SCRIPT_NONCE_ATTRIBUTE = "scriptNonce";

    @ModelAttribute("structurizrConfiguration")
    Configuration getConfiguration() {
        return Configuration.getInstance();
    }

    @ModelAttribute
    void addSecurityHeaders(HttpServletResponse response, ModelMap model) {
        response.addHeader(REFERER_POLICY_HEADER, REFERER_POLICY_VALUE);

        String nonce = Base64.getEncoder().encodeToString(new RandomGuidGenerator().generate().getBytes(StandardCharsets.UTF_8));
        model.addAttribute(SCRIPT_NONCE_ATTRIBUTE, nonce);

        response.addHeader(CONTENT_SECURITY_POLICY_HEADER, String.format("script-src 'self' 'nonce-%s'; worker-src blob:", nonce));
    }

    @ModelAttribute
    void addCommonAttributes(ModelMap model) {
        model.addAttribute("timeZone", TimeZone.getDefault().getID());
        model.addAttribute("showHeader", false);
        model.addAttribute("showFooter", false);
        model.addAttribute("version", new Version());

        model.addAttribute("authenticated", false);
        model.addAttribute("authenticationEnabled", false);
        model.addAttribute("pageTitle", "Structurizr");
    }

}