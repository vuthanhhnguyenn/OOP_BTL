package com.clinic.triage.model;

public class SymptomRule {
    private final long id;
    private final String keyword;
    private final String departmentCode;
    private final String explanation;

    public SymptomRule(long id, String keyword, String departmentCode, String explanation) {
        this.id = id;
        this.keyword = keyword;
        this.departmentCode = departmentCode;
        this.explanation = explanation;
    }

    public long getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getExplanation() {
        return explanation;
    }
}
