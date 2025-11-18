package com.clinic.triage.model;

public class Department {
    private final String code;
    private final String name;
    private final String queuePrefix;

    public Department(String code, String name, String queuePrefix) {
        this.code = code;
        this.name = name;
        this.queuePrefix = queuePrefix;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
