package com.structurizr.api;

import java.util.Base64;

public final class HmacAuthorizationHeader {

    private final String apiKey;
    private final String hmac;

    HmacAuthorizationHeader(String apiKey, String hmac) {
        this.apiKey = apiKey;
        this.hmac = hmac;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getHmac() {
        return hmac;
    }

    public String format() {
        return apiKey + ":" + Base64.getEncoder().encodeToString(hmac.getBytes());
    }

    public static HmacAuthorizationHeader parse(String s) {
        String[] parts = s.split(":");
        if (parts.length == 2) {
            String apiKey = parts[0];
            String hmac = new String(Base64.getDecoder().decode(parts[1]));

            return new HmacAuthorizationHeader(apiKey, hmac);
        } else {
            throw new IllegalArgumentException("Invalid authorization header");
        }
    }

}