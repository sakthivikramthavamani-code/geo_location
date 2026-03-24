package com.georeport.controller;

import com.georeport.dto.AnalyticsResponse;
import com.georeport.dto.ApiResponse;
import com.georeport.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for analytics endpoints (Admin only).
 */
@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get comprehensive analytics data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        AnalyticsResponse analytics = analyticsService.getAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    /**
     * Get issue trends (daily counts for last N days)
     */
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTrends(
            @RequestParam(defaultValue = "30") int days) {

        Map<String, Long> trends = analyticsService.getTrends(days);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    /**
     * Get category distribution
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCategoryDistribution() {
        Map<String, Long> distribution = analyticsService.getCategoryDistribution();
        return ResponseEntity.ok(ApiResponse.success(distribution));
    }

    /**
     * Get average resolution times by category
     */
    @GetMapping("/resolution-times")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getResolutionTimes() {
        Map<String, Double> times = analyticsService.getResolutionTimes();
        return ResponseEntity.ok(ApiResponse.success(times));
    }

    /**
     * Get hotspot data for heatmap
     */
    @GetMapping("/hotspots")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse.HotspotData>>> getHotspots() {
        List<AnalyticsResponse.HotspotData> hotspots = analyticsService.getHotspotData();
        return ResponseEntity.ok(ApiResponse.success(hotspots));
    }
}
