package com.structurizr.server.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class HttpUnauthorizedException extends RuntimeException {

    public HttpUnauthorizedException(String message) {
        super(message);
    }

    public HttpUnauthorizedException(Throwable cause) {
        this(cause.getMessage());
    }

}