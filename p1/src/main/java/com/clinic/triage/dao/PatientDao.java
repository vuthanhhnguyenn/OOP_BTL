package com.clinic.triage.dao;

import com.clinic.triage.db.DatabaseInitializer;
import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.Patient;
import com.clinic.triage.model.PatientStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDao {

    public Patient insert(String name, int age, String symptoms, String departmentCode, String queueNumber) {
        String sql = "INSERT INTO patients(full_name, age, symptoms, department_code, queue_number, exam_note, status, created_at, updated_at) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String now = DatabaseInitializer.now();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setInt(2, age);
            statement.setString(3, symptoms);
            statement.setString(4, departmentCode);
            statement.setString(5, queueNumber);
            statement.setString(6, null);
            statement.setString(7, PatientStatus.WAITING.name());
            statement.setString(8, now);
            statement.setString(9, now);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Patient(id, name, age, symptoms, departmentCode, queueNumber, null, PatientStatus.WAITING, now, now);
                }
            }
            throw new IllegalStateException("Không nhận được id bệnh nhân mới");
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu bệnh nhân", e);
        }
    }

    public List<Patient> findWaitingByDepartment(String departmentCode) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE department_code = ? AND status = ? ORDER BY id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, departmentCode);
            statement.setString(2, PatientStatus.WAITING.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    patients.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lấy danh sách bệnh nhân chờ", e);
        }
        return patients;
    }

    public Optional<Patient> findByQueueNumber(String queueNumber) {
        String sql = "SELECT * FROM patients WHERE queue_number = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, queueNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm bệnh nhân theo số thứ tự", e);
        }
        return Optional.empty();
    }

    public void updateStatus(long patientId, PatientStatus status) {
        String sql = "UPDATE patients SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, DatabaseInitializer.now());
            statement.setLong(3, patientId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái bệnh nhân", e);
        }
    }

    public void updateExamNote(long patientId, String note) {
        String sql = "UPDATE patients SET exam_note = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, note);
            statement.setString(2, DatabaseInitializer.now());
            statement.setLong(3, patientId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu kết quả khám bệnh", e);
        }
    }

    private Patient map(ResultSet resultSet) throws SQLException {
        return new Patient(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getInt("age"),
                resultSet.getString("symptoms"),
                resultSet.getString("department_code"),
                resultSet.getString("queue_number"),
                resultSet.getString("exam_note"),
                PatientStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at"));
    }
}
