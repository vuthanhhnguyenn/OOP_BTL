package com.clinic.triage.model;

public class Prescription {
    private final long id;
    private final long patientId;
    private final long doctorId;
    private final String departmentCode;
    private final String queueNumber;
    private final String content;
    private final String createdAt;

    public Prescription(long id, long patientId, long doctorId, String departmentCode,
                        String queueNumber, String content, String createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.departmentCode = departmentCode;
        this.queueNumber = queueNumber;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getPatientId() {
        return patientId;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
