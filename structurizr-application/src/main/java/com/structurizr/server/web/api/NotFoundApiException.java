package com.structurizr.server.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundApiException extends RuntimeException {

    public NotFoundApiException() {
        super("404");
    }

}