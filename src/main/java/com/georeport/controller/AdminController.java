package com.georeport.controller;

import com.georeport.dto.*;
import com.georeport.entity.IssueStatus;
import com.georeport.entity.User;
import com.georeport.service.AuthService;
import com.georeport.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for admin operations.
 * Handles issue management, status updates, and dashboard statistics.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private AuthService authService;

    /**
     * Get dashboard statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = issueService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get all issues with advanced filters
     * GET /api/admin/issues
     */
    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAllIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueResponse> issues = issueService.getAllIssues(status, categoryId, ward, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Update issue status
     * PUT /api/admin/issues/{id}/status
     */
    @PutMapping("/issues/{id}/status")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {

        User admin = authService.getCurrentUser();
        IssueResponse response = issueService.updateIssueStatus(id, request, admin);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    /**
     * Get issues by status
     * GET /api/admin/issues/by-status/{status}
     */
    @GetMapping("/issues/by-status/{status}")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getIssuesByStatus(
            @PathVariable IssueStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IssueResponse> issues = issueService.getAllIssues(status, null, null, null, null, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Get available wards
     * GET /api/admin/wards
     */
    @GetMapping("/wards")
    public ResponseEntity<ApiResponse<List<String>>> getWards() {
        DashboardStats stats = issueService.getDashboardStats();
        List<String> wards = stats.getIssuesByWard().keySet().stream().sorted().toList();
        return ResponseEntity.ok(ApiResponse.success(wards));
    }
}
