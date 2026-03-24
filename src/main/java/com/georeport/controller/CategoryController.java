package com.georeport.controller;

import com.georeport.dto.ApiResponse;
import com.georeport.dto.CategoryResponse;
import com.georeport.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for category operations.
 * Public endpoint for retrieving issue categories.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private IssueService issueService;

    /**
     * Get all active categories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = issueService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
