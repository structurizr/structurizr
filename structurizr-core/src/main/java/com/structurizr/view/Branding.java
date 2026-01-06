package com.structurizr.view;

import com.structurizr.util.ImageUtils;
import com.structurizr.util.StringUtils;

/**
 * A wrapper for the logo associated with a corporate branding.
 */
public final class Branding {

    private String logo;

    Branding() {
    }

    public String getLogo() {
        return logo;
    }

    /**
     * Sets the URL of an image representing a logo.
     *
     * @param logo   a URL or data URI as a String
     */
    public void setLogo(String logo) {
        if (StringUtils.isNullOrEmpty(logo)) {
            this.logo = null;
        } else {
            ImageUtils.validateImage(logo);
            this.logo = logo.trim();
        }
    }

}