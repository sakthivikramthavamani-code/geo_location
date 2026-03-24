package com.georeport.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for analytics data.
 */
@Data
@Builder
public class AnalyticsResponse {

    /**
     * Issue count by status
     */
    private Map<String, Long> byStatus;

    /**
     * Issue count by category
     */
    private Map<String, Long> byCategory;

    /**
     * Issue count by priority
     */
    private Map<String, Long> byPriority;

    /**
     * Issue count by ward
     */
    private Map<String, Long> byWard;

    /**
     * Issues over time (for trend chart)
     * Key: date string (YYYY-MM-DD), Value: count
     */
    private Map<String, Long> issueTrends;

    /**
     * Average resolution time in hours by category
     */
    private Map<String, Double> avgResolutionTimeByCategory;

    /**
     * Hotspot data for heatmap (lat, lng, intensity)
     */
    private List<HotspotData> hotspots;

    /**
     * Overall statistics
     */
    private OverallStats overall;

    @Data
    @Builder
    public static class HotspotData {
        private Double latitude;
        private Double longitude;
        private Long count;
    }

    @Data
    @Builder
    public static class OverallStats {
        private Long totalIssues;
        private Long resolvedThisMonth;
        private Long newThisWeek;
        private Double avgResolutionTimeHours;
        private Double resolutionRate; // percentage
    }
}
