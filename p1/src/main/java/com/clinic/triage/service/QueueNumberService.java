package com.clinic.triage.service;

import com.clinic.triage.dao.DepartmentDao;
import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueueNumberService {
    private final DepartmentDao departmentDao;

    public QueueNumberService() {
        this(new DepartmentDao());
    }

    public QueueNumberService(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public String nextQueueNumber(String departmentCode) {
        Department department = departmentDao.findByCode(departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Khoa không tồn tại: " + departmentCode));
        String lastNumber = fetchLatestQueueNumber(departmentCode);
        int next = 1;
        if (lastNumber != null && lastNumber.startsWith(department.getQueuePrefix())) {
            int dash = lastNumber.indexOf('-');
            if (dash >= 0) {
                try {
                    next = Integer.parseInt(lastNumber.substring(dash + 1)) + 1;
                } catch (NumberFormatException ignored) {
                    next = 1;
                }
            }
        }
        return String.format("%s-%02d", department.getQueuePrefix(), next);
    }

    private String fetchLatestQueueNumber(String departmentCode) {
        String sql = "SELECT queue_number FROM patients WHERE department_code = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, departmentCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("queue_number");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lấy số thứ tự cuối", e);
        }
        return null;
    }
}
