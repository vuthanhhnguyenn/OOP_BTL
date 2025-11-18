package com.clinic.triage.service;

import com.clinic.triage.dao.DoctorDao;
import com.clinic.triage.model.Doctor;
import com.clinic.triage.util.PasswordHasher;

import java.util.Optional;

public class AuthService {
    private final DoctorDao doctorDao;

    public AuthService() {
        this(new DoctorDao());
    }

    public AuthService(DoctorDao doctorDao) {
        this.doctorDao = doctorDao;
    }

    public Optional<Doctor> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return doctorDao.findByUsername(username.trim())
                .filter(record -> PasswordHasher.matches(password, record.passwordHash()))
                .map(DoctorDao.DoctorRecord::doctor);
    }
}
