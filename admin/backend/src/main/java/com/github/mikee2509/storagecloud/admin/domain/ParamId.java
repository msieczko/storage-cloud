package com.github.mikee2509.storagecloud.admin.domain;

public enum ParamId {
    SID,
    USERNAME,
    PASSWORD,
    PATH,
    MSG,
    PASS,
    FIRST_NAME,
    LAST_NAME;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
