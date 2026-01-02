package com.structurizr.util;

public final class DocumentationScope {

    private static final String SEPARATOR = "/";
    private static final String WORKSPACE_SCOPE = "*";

    public static String format(String softwareSystem, String container, String component) {
        if (softwareSystem != null && container != null && component != null) {
            return softwareSystem + SEPARATOR + container + SEPARATOR + component;
        } else if (softwareSystem != null && container != null) {
            return softwareSystem + SEPARATOR + container;
        } else if (softwareSystem != null) {
            return softwareSystem;
        } else {
            return WORKSPACE_SCOPE;
        }
    }

}