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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for issue management.
 * Handles issue creation, retrieval, and status tracking.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private AuthService authService;

    /**
     * Create a new issue
     * POST /api/issues
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @Valid @RequestPart("issue") CreateIssueRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        User reporter = authService.getCurrentUser();
        IssueResponse response = issueService.createIssue(request, reporter, images);
        return ResponseEntity.ok(ApiResponse.success("Issue reported successfully", response));
    }

    /**
     * Get issue by ID
     * GET /api/issues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssue(@PathVariable Long id) {
        IssueResponse response = issueService.getIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete issue
     * DELETE /api/issues/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteIssue(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        issueService.deleteIssue(id, user);
        return ResponseEntity.ok(ApiResponse.success("Issue deleted successfully", null));
    }

    /**
     * Update issue details
     * PUT /api/issues/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssue(
            @PathVariable Long id,
            @RequestBody @Valid CreateIssueRequest request) {
        User user = authService.getCurrentUser();
        IssueResponse response = issueService.updateIssueDetails(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Issue updated successfully", response));
    }

    /**
     * Get issues for current user
     * GET /api/issues/my-issues
     */
    @GetMapping("/my-issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getMyIssues() {
        User user = authService.getCurrentUser();
        List<IssueResponse> issues = issueService.getIssuesByReporter(user.getId());
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Get status history for an issue
     * GET /api/issues/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<StatusHistoryResponse>>> getStatusHistory(@PathVariable Long id) {
        List<StatusHistoryResponse> history = issueService.getStatusHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Get nearby issues (public endpoint)
     * GET /api/issues/public/nearby
     */
    @GetMapping("/public/nearby")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getNearbyIssues(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radius) {

        List<IssueResponse> issues = issueService.getNearbyIssues(latitude, longitude, radius);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Get issues within bounding box (for map viewport)
     * GET /api/issues/public/bounds
     */
    @GetMapping("/public/bounds")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getIssuesInBounds(
            @RequestParam double minLng,
            @RequestParam double minLat,
            @RequestParam double maxLng,
            @RequestParam double maxLat) {

        List<IssueResponse> issues = issueService.getIssuesInBounds(minLng, minLat, maxLng, maxLat);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Get all issues with filters and pagination (public)
     * GET /api/issues/public/all
     */
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAllIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String ward,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IssueResponse> issues = issueService.getAllIssues(status, categoryId, ward, null, null, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Advanced search endpoint with multiple filters
     * POST /api/issues/search
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> searchIssues(
            @RequestBody IssueSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Create sort
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy() != null ? request.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueResponse> issues = issueService.searchIssues(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Public advanced search endpoint (no auth required)
     * POST /api/issues/public/search
     */
    @PostMapping("/public/search")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> publicSearchIssues(
            @RequestBody IssueSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy() != null ? request.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueResponse> issues = issueService.searchIssues(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }
}
