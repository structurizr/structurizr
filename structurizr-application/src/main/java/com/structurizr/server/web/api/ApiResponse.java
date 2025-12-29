package com.structurizr.server.web.api;

public class ApiResponse {

    private boolean success = false;
    private String message = "";

    public ApiResponse(String message) {
        this(true, message);
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    ApiResponse(Exception e) {
        this(false, e.getMessage());
    }

    public boolean isSuccess() {
        return success;
    }

    void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

}