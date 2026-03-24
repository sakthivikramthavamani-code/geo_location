package com.georeport.dto;

import com.georeport.entity.IssuePriority;
import com.georeport.entity.IssueStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for advanced issue search with multiple filter options.
 */
@Data
public class IssueSearchRequest {

    /**
     * Keyword search in title and description
     */
    private String keyword;

    /**
     * Filter by multiple statuses
     */
    private List<IssueStatus> statuses;

    /**
     * Filter by multiple categories
     */
    private List<Long> categoryIds;

    /**
     * Filter by multiple priorities
     */
    private List<IssuePriority> priorities;

    /**
     * Filter by ward
     */
    private String ward;

    /**
     * Start date for date range filter
     */
    private LocalDateTime startDate;

    /**
     * End date for date range filter
     */
    private LocalDateTime endDate;

    /**
     * Center latitude for radius search
     */
    private Double latitude;

    /**
     * Center longitude for radius search
     */
    private Double longitude;

    /**
     * Radius in meters for proximity search
     */
    private Double radiusMeters;

    /**
     * Sort field (createdAt, priority, status)
     */
    private String sortBy = "createdAt";

    /**
     * Sort direction (asc, desc)
     */
    private String sortDirection = "desc";
}
