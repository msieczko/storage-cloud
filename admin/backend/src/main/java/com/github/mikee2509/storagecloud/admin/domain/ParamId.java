package com.github.mikee2509.storagecloud.admin.domain;

public enum ParamId {
    SID,
    USERNAME,
    PASSWORD;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
