package com.georeport.dto;

import lombok.*;
import java.util.Map;

/**
 * DTO for dashboard statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    private Long totalIssues;
    private Long submittedCount;
    private Long inProgressCount;
    private Long resolvedCount;
    private Long rejectedCount;
    private Map<String, Long> issuesByCategory;
    private Map<String, Long> issuesByWard;
}
