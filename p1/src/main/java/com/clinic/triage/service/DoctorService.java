package com.clinic.triage.service;

import com.clinic.triage.dao.PatientDao;
import com.clinic.triage.dao.PrescriptionDao;
import com.clinic.triage.model.Doctor;
import com.clinic.triage.model.Patient;
import com.clinic.triage.model.PatientStatus;
import com.clinic.triage.model.Prescription;

import java.util.List;

public class DoctorService {
    private final PatientDao patientDao;
    private final PrescriptionDao prescriptionDao;

    public DoctorService() {
        this(new PatientDao(), new PrescriptionDao());
    }

    public DoctorService(PatientDao patientDao, PrescriptionDao prescriptionDao) {
        this.patientDao = patientDao;
        this.prescriptionDao = prescriptionDao;
    }

    public List<Patient> findWaitingPatients(String departmentCode) {
        return patientDao.findWaitingByDepartment(departmentCode);
    }

    public void markAsExamined(Patient patient, String examNote) {
        patientDao.updateExamNote(patient.getId(), examNote);
        patientDao.updateStatus(patient.getId(), PatientStatus.DONE);
    }

    public void saveExamNote(Patient patient, String examNote) {
        patientDao.updateExamNote(patient.getId(), examNote);
    }

    public Prescription createPrescription(Doctor doctor, Patient patient, String content) {
        String cleanedContent = content == null ? "" : content.trim();
        if (cleanedContent.isBlank()) {
            throw new IllegalArgumentException("Đơn thuốc chưa được nhập");
        }
        Prescription prescription = prescriptionDao.save(
                patient.getId(),
                doctor.getId(),
                doctor.getDepartmentCode(),
                patient.getQueueNumber(),
                cleanedContent);
        patientDao.updateStatus(patient.getId(), PatientStatus.DONE);
        return prescription;
    }
}
