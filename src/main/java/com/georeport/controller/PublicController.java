package com.georeport.controller;

import com.georeport.dto.ApiResponse;
import com.georeport.dto.DashboardStats;
import com.georeport.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for public endpoints.
 * Provides limited public data for unauthenticated users.
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    @Autowired
    private IssueService issueService;

    /**
     * Get public statistics for the landing page
     * GET /api/public/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStats() {
        DashboardStats stats = issueService.getDashboardStats();

        // Return limited public data only
        Map<String, Object> publicStats = new HashMap<>();
        publicStats.put("totalIssues", stats.getTotalIssues());
        publicStats.put("resolvedCount", stats.getResolvedCount());
        publicStats.put("inProgressCount", stats.getInProgressCount());
        publicStats.put("submittedCount", stats.getSubmittedCount());

        return ResponseEntity.ok(ApiResponse.success(publicStats));
    }
}
