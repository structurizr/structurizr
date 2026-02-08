package com.structurizr.model;

import com.structurizr.util.StringUtils;
/**
 * Represents an architectural perspective, that can be applied to elements and relationships.
 * See https://www.viewpoints-and-perspectives.info/home/perspectives/ for more details of this concept.
 */
public final class Perspective implements Comparable<Perspective> {

    private String name;
    private String description;
    private String value;

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

    public void setValue(String value) {
        this.value = value;
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