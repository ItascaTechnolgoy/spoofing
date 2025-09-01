package com.itasca.spoofing.model;

public enum ProfileType {
    SINGLE("single"),
    GROUP("group");

    private final String value;

    ProfileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}