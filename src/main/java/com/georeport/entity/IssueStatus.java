package com.georeport.entity;

/**
 * Enumeration representing the status of an issue.
 */
public enum IssueStatus {
    SUBMITTED("Submitted", "Issue has been submitted and is awaiting review"),
    IN_PROGRESS("In Progress", "Issue is being worked on"),
    RESOLVED("Resolved", "Issue has been resolved"),
    REJECTED("Rejected", "Issue has been rejected"),
    CLOSED("Closed", "Issue has been closed");

    private final String displayName;
    private final String description;

    IssueStatus(String displayName, String description) {
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
