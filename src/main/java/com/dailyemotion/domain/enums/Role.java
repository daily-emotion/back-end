package com.dailyemotion.domain.enums;

public enum Role {
    USER,
    ADMIN;

    public String getAuthority() {
        return name();
    }
}
