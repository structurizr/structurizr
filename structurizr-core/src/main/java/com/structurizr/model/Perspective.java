package com.structurizr.model;

import com.structurizr.util.StringUtils;
import com.structurizr.util.Url;

/**
 * Represents an architectural perspective, that can be applied to elements and relationships.
 * See https://www.viewpoints-and-perspectives.info/home/perspectives/ for more details of this concept.
 *
 * Two types of perspectives are supported:
 *
 * 1. Static perspective: has a static value.
 * 2. Dynamic perspective: has a dynamic value, retrieved via a URL.
 */
public final class Perspective implements Comparable<Perspective> {

    private String name;
    private String description;
    private String value;
    private String url;

    Perspective() {
    }

    /**
     * Creates a perspective with the given name.
     *
     * @param name          the perspective name
     */
    public Perspective(String name) {
        this.name = name;
    }

    /**
     * Creates a static perspective, with the given name, description, and value.
     *
     * @param name          the perspective name
     * @param description   the perspective description
     * @param value         the perspective value (optional)
     */
    public Perspective(String name, String description, String value) {
        setName(name);
        this.description = description;
        this.value = value;
    }

    /**
     * Creates a dynamic perspective, the value of which is sourced from a URL.
     *
     * @param name          the perspective name
     * @param url           the perspective URL
     */
    public Perspective(String name, String url) {
        setName(name);
        setUrl(url);
    }

    /**
     * Gets the name of this perspective (e.g. "Security").
     *
     * @return  the name of this perspective, as a String
     */
    public String getName() {
        return name;
    }

    void setName(String name) {
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Perspective name must be specified");
        }

        this.name = name;
    }

    /**
     * Gets the description of this perspective.
     *
     * @return  the description of this perspective, as a String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this perspective.
     *
     * @param description       the description, as a String
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the value of this perspective.
     *
     * @return  the value of this perspective, as a String
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this perspective.
     *
     * @param value     the value, as a String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the URL of this perspective.
     *
     * @return  the URL of this perspective, as a String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of this perspective.
     *
     * @param url       the URL as a String
     */
    public void setUrl(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("URL must be specified");
        } else if (Url.isUrl(url)) {
            this.url = url;
        } else {
            throw new IllegalArgumentException(url + " is not a valid URL.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Perspective that = (Perspective) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public int compareTo(Perspective perspective) {
        return getName().compareTo(perspective.getName());
    }

}