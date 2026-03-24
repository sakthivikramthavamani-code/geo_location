package com.georeport.service;

import com.georeport.entity.IssueCategory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for automatically routing issues to the correct department
 * based on the issue category name.
 *
 * Routing map is sourced from DataInitializer.java categories:
 *   Road Damage       → Road Department
 *   Water Leakage     → Water Department
 *   Garbage Overflow  → Sanitation Department
 *   Street Light      → Electricity Department
 *   Sanitation        → Sanitation Department
 *   Traffic           → Traffic Department
 *   Public Property   → Public Works Department
 *   Other             → General Administration
 */
@Service
public class DepartmentRoutingService {

    private static final Map<String, String> ROUTING_MAP = Map.of(
            "Road Damage",      "Road Department",
            "Water Leakage",    "Water Department",
            "Garbage Overflow", "Sanitation Department",
            "Street Light",     "Electricity Department",
            "Sanitation",       "Sanitation Department",
            "Traffic",          "Traffic Department",
            "Public Property",  "Public Works Department",
            "Other",            "General Administration"
    );

    /**
     * Returns the department name for a given issue category.
     * Falls back to "General Administration" if no mapping is found.
     *
     * @param category the IssueCategory of the complaint
     * @return the name of the assigned department
     */
    public String route(IssueCategory category) {
        if (category == null || category.getName() == null) {
            return "General Administration";
        }
        return ROUTING_MAP.getOrDefault(category.getName(), "General Administration");
    }

    /**
     * Returns the full routing map (useful for admin reference or future dynamic config).
     */
    public Map<String, String> getRoutingMap() {
        return ROUTING_MAP;
    }
}
