package com.structurizr.server.component.search;

public class SearchComponentException extends RuntimeException {

    public SearchComponentException(String message) {
        super(message);
    }

    public SearchComponentException(String message, Throwable cause) {
        super(message, cause);
    }

}