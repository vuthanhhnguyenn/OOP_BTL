package com.clinic.triage.dao;

import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDao {

    public Optional<Department> findByCode(String code) {
        String sql = "SELECT code, name, queue_prefix FROM departments WHERE code = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể truy vấn khoa", e);
        }
        return Optional.empty();
    }

    public List<Department> findAll() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT code, name, queue_prefix FROM departments ORDER BY name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                departments.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách khoa", e);
        }
        return departments;
    }

    private Department map(ResultSet resultSet) throws SQLException {
        return new Department(
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getString("queue_prefix"));
    }
}
