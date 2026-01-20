package com.structurizr.view;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.structurizr.util.ImageUtils;
import com.structurizr.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;

public final class Theme {

    private String name;
    private String description;
    private Collection<ElementStyle> elements = new LinkedList<>();
    private Collection<RelationshipStyle> relationships = new LinkedList<>();
    private String logo;
    private String license;

    Theme() {
    }

    Theme(Collection<ElementStyle> elements, Collection<RelationshipStyle> relationships) {
        this.elements = elements;
        this.relationships = relationships;
    }

    Theme(String name, String description, Collection<ElementStyle> elements, Collection<RelationshipStyle> relationships) {
        this.name = name;
        this.description = description;
        this.elements = elements;
        this.relationships = relationships;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @JsonGetter
    public Collection<ElementStyle> getElements() {
        return elements;
    }

    void setElements(Collection<ElementStyle> elements) {
        this.elements = elements;
    }

    @JsonGetter
    public Collection<RelationshipStyle> getRelationships() {
        return relationships;
    }

    void setRelationships(Collection<RelationshipStyle> relationships) {
        this.relationships = relationships;
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

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

}