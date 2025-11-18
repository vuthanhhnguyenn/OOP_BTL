package com.clinic.triage.service;

import com.clinic.triage.dao.DepartmentDao;
import com.clinic.triage.dao.PatientDao;
import com.clinic.triage.dao.PrescriptionDao;
import com.clinic.triage.model.Department;
import com.clinic.triage.model.Patient;
import com.clinic.triage.model.Prescription;

import java.util.Objects;
import java.util.Optional;
import java.util.List;

public class TriageService {
    private final SymptomClassifier classifier;
    private final QueueNumberService queueNumberService;
    private final PatientDao patientDao;
    private final PrescriptionDao prescriptionDao;
    private final DepartmentDao departmentDao;

    public TriageService() {
        this(new SymptomClassifier(), new QueueNumberService(), new PatientDao(), new PrescriptionDao(), new DepartmentDao());
    }

    public TriageService(SymptomClassifier classifier, QueueNumberService queueNumberService,
                         PatientDao patientDao, PrescriptionDao prescriptionDao, DepartmentDao departmentDao) {
        this.classifier = classifier;
        this.queueNumberService = queueNumberService;
        this.patientDao = patientDao;
        this.prescriptionDao = prescriptionDao;
        this.departmentDao = departmentDao;
    }

    public PatientRegistrationResult registerPatient(String name, int age, String symptoms, String preferredDepartmentCode) {
        validatePatientInfo(name, age, symptoms);
        Department department = resolveDepartment(symptoms, preferredDepartmentCode);
        String queueNumber = queueNumberService.nextQueueNumber(department.getCode());
        Patient patient = patientDao.insert(name.trim(), age, symptoms.trim(), department.getCode(), queueNumber);
        return new PatientRegistrationResult(patient, department);
    }

    public Optional<Prescription> findPrescriptionByQueueNumber(String queueNumber) {
        if (queueNumber == null || queueNumber.isBlank()) {
            return Optional.empty();
        }
        return prescriptionDao.findLatestByQueueNumber(queueNumber.trim());
    }

    public Optional<Patient> findPatientByQueueNumber(String queueNumber) {
        if (queueNumber == null || queueNumber.isBlank()) {
            return Optional.empty();
        }
        return patientDao.findByQueueNumber(queueNumber.trim());
    }

    public List<Department> listDepartments() {
        return departmentDao.findAll();
    }

    private Department resolveDepartment(String symptoms, String preferredDepartmentCode) {
        if (preferredDepartmentCode != null && !preferredDepartmentCode.isBlank()) {
            return departmentDao.findByCode(preferredDepartmentCode.trim())
                    .orElseGet(() -> classifier.classify(symptoms));
        }
        return classifier.classify(symptoms);
    }

    private void validatePatientInfo(String name, int age, String symptoms) {
        Objects.requireNonNull(name, "Tên không được để trống");
        Objects.requireNonNull(symptoms, "Triệu chứng không được để trống");
        if (name.isBlank() || symptoms.isBlank()) {
            throw new IllegalArgumentException("Thông tin nhập chưa hợp lệ");
        }
        if (age <= 0 || age > 120) {
            throw new IllegalArgumentException("Tuổi phải nằm trong khoảng 1 - 120");
        }
    }

    public record PatientRegistrationResult(Patient patient, Department department) {
    }
}
