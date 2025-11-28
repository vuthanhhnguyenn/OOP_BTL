package com.clinic.triage.dao;

import com.clinic.triage.db.DatabaseInitializer;
import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.Prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class PrescriptionDao {

    public Prescription save(long patientId, long doctorId, String departmentCode,
                             String queueNumber, String content) {
        String sql = "INSERT INTO prescriptions(patient_id, doctor_id, department_code, queue_number, content, created_at) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        String now = DatabaseInitializer.now();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, patientId);
            statement.setLong(2, doctorId);
            statement.setString(3, departmentCode);
            statement.setString(4, queueNumber);
            statement.setString(5, content);
            statement.setString(6, now);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Prescription(id, patientId, doctorId, departmentCode, queueNumber, content, now);
                }
            }
            throw new IllegalStateException("Không nhận được id đơn thuốc");
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu đơn thuốc", e);
        }
    }

    public Optional<Prescription> findLatestByQueueNumber(String queueNumber) {
        String sql = "SELECT * FROM prescriptions WHERE queue_number = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, queueNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm đơn thuốc", e);
        }
        return Optional.empty();
    }

    private Prescription map(ResultSet resultSet) throws SQLException {
        return new Prescription(
                resultSet.getLong("id"),
                resultSet.getLong("patient_id"),
                resultSet.getLong("doctor_id"),
                resultSet.getString("department_code"),
                resultSet.getString("queue_number"),
                resultSet.getString("content"),
                resultSet.getString("created_at"));
    }
}
