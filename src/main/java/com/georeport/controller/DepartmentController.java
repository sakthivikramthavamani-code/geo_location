package com.georeport.controller;

import com.georeport.dto.ApiResponse;
import com.georeport.dto.IssueResponse;
import com.georeport.dto.UpdateStatusRequest;
import com.georeport.entity.User;
import com.georeport.service.AuthService;
import com.georeport.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for department staff operations.
 * Each department sees ONLY their own complaints.
 * Accessible to users with ROLE_ADMIN or ROLE_DEPARTMENT.
 */
@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private AuthService authService;

    /**
     * Get complaints for a specific department.
     * Filters issues strictly by department name.
     * GET /api/department/issues?dept=Road+Department
     */
    @GetMapping("/issues")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPARTMENT')")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getIssuesByDepartment(
            @RequestParam String dept) {
        List<IssueResponse> issues = issueService.getIssuesByDepartment(dept);
        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    /**
     * Update issue status (by department staff).
     * PUT /api/department/issues/{id}/status
     */
    @PutMapping("/issues/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPARTMENT')")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        User currentUser = authService.getCurrentUser();
        IssueResponse response = issueService.updateIssueStatus(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    /**
     * Get complaint counts per department (routing stats overview).
     * GET /api/department/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPARTMENT')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDepartmentStats() {
        Map<String, Long> stats = issueService.getDepartmentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
