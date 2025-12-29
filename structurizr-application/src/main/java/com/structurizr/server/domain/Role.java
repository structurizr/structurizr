package com.structurizr.server.domain;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {

    private final String name;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }

}