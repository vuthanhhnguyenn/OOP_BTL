package com.clinic.triage.model;

public class Doctor {
    private final long id;
    private final String username;
    private final String displayName;
    private final String departmentCode;

    public Doctor(long id, String username, String displayName, String departmentCode) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.departmentCode = departmentCode;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
}
