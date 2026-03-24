package com.georeport.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for issue response with all details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {

    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String ward;
    private String landmark;
    private String status;
    private String statusDisplayName;
    private String priority;
    private String department;

    // Category info
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    // Reporter info
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;

    // Direct contact info from form
    private String contactEmail;
    private String contactPhone;

    // Assigned admin info
    private Long assignedToId;
    private String assignedToName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // Resolution
    private String resolutionNotes;
    private String rejectionReason;

    // Images
    private List<IssueImageResponse> images;

    // Engagement metrics
    private Long voteCount;
    private Long commentCount;
}
