package com.structurizr.util;

public class DocumentationScope {

    private static final String WORKSPACE_SCOPE = "*";

    public static String format(String softwareSystem, String container, String component) {
        if (softwareSystem != null && container != null && component != null) {
            return softwareSystem + "/" + container + "/" + component;
        } else if (softwareSystem != null && container != null) {
            return softwareSystem + "/" + container;
        } else if (softwareSystem != null) {
            return softwareSystem;
        } else {
            return WORKSPACE_SCOPE;
        }
    }

}