package com.clinic.triage.service;

import com.clinic.triage.dao.DepartmentDao;
import com.clinic.triage.dao.SymptomRuleDao;
import com.clinic.triage.model.Department;
import com.clinic.triage.model.SymptomRule;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SymptomClassifier {
    private final SymptomRuleDao ruleDao;
    private final DepartmentDao departmentDao;
    private Map<String, Department> departmentCache;
    private List<SymptomRule> cachedRules;

    public SymptomClassifier() {
        this(new SymptomRuleDao(), new DepartmentDao());
    }

    public SymptomClassifier(SymptomRuleDao ruleDao, DepartmentDao departmentDao) {
        this.ruleDao = ruleDao;
        this.departmentDao = departmentDao;
    }

    public synchronized Department classify(String description) {
        ensureCache();
        String normalized = normalize(description);
        for (SymptomRule rule : cachedRules) {
            String keyword = normalize(rule.getKeyword());
            if (!keyword.isBlank() && normalized.contains(keyword)) {
                Department match = departmentCache.get(rule.getDepartmentCode());
                if (match != null) {
                    return match;
                }
            }
        }
        Department fallback = departmentCache.get("NOI");
        if (fallback == null) {
            fallback = departmentCache.values().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Chưa có dữ liệu khoa trong hệ thống"));
        }
        return fallback;
    }

    public synchronized List<SymptomRule> getCachedRules() {
        ensureCache();
        return cachedRules;
    }

    private void ensureCache() {
        if (departmentCache == null) {
            departmentCache = new HashMap<>();
            departmentDao.findAll().forEach(department -> departmentCache.put(department.getCode(), department));
        }
        if (cachedRules == null) {
            cachedRules = ruleDao.findAll();
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.forLanguageTag("vi-VN")).trim();
    }
}
