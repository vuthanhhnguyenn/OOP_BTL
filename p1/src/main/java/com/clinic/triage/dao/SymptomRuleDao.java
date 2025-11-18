package com.clinic.triage.dao;

import com.clinic.triage.db.DatabaseManager;
import com.clinic.triage.model.SymptomRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SymptomRuleDao {

    public List<SymptomRule> findAll() {
        List<SymptomRule> rules = new ArrayList<>();
        String sql = "SELECT id, keyword, department_code, explanation FROM symptom_rules ORDER BY keyword";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rules.add(new SymptomRule(
                        resultSet.getLong("id"),
                        resultSet.getString("keyword"),
                        resultSet.getString("department_code"),
                        resultSet.getString("explanation")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải bảng quy tắc triệu chứng", e);
        }
        return rules;
    }
}
