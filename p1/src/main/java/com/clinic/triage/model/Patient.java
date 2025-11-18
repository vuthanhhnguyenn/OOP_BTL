package com.clinic.triage.model;

public class Patient {
    private final long id;
    private final String fullName;
    private final int age;
    private final String symptoms;
    private final String departmentCode;
    private final String queueNumber;
    private final String examNote;
    private final PatientStatus status;
    private final String createdAt;
    private final String updatedAt;

    public Patient(long id, String fullName, int age, String symptoms, String departmentCode,
                   String queueNumber, String examNote, PatientStatus status, String createdAt, String updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.symptoms = symptoms;
        this.departmentCode = departmentCode;
        this.queueNumber = queueNumber;
        this.examNote = examNote;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getExamNote() {
        return examNote;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return queueNumber + " - " + fullName + " (" + age + "t)";
    }
}
