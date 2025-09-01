package com.itasca.spoofing.model;


import com.fasterxml.jackson.annotation.JsonValue;

public enum IPType {
    FIXED("fixed"),
    RANDOM("random");

    private final String value;

    IPType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IPType fromValue(String value) {
        for (IPType type : IPType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown IPType: " + value);
    }
}