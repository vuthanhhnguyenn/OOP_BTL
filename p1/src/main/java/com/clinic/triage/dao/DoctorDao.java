package com.clinic.triage.dao;

import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DoctorDao {

    public Optional<DoctorRecord> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, display_name, department_code FROM doctors WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Doctor doctor = new Doctor(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("display_name"),
                            resultSet.getString("department_code"));
                    String passwordHash = resultSet.getString("password_hash");
                    return Optional.of(new DoctorRecord(doctor, passwordHash));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm bác sĩ", e);
        }
        return Optional.empty();
    }

    public record DoctorRecord(Doctor doctor, String passwordHash) {
    }
}
