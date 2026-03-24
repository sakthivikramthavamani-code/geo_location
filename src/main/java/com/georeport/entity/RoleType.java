package com.georeport.entity;

/**
 * Enumeration of user roles in the system.
 * Used for role-based access control.
 */
public enum RoleType {
    ROLE_CITIZEN("Citizen", "Regular citizen who can report and track issues"),
    ROLE_ADMIN("Administrator", "Municipal administrator who manages and resolves issues"),
    ROLE_DEPARTMENT("Department Staff", "Department staff who handle complaints assigned to their department");


    private final String displayName;
    private final String description;

    RoleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
