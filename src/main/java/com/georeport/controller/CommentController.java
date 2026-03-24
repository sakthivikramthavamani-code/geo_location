package com.georeport.controller;

import com.georeport.dto.ApiResponse;
import com.georeport.dto.CommentResponse;
import com.georeport.dto.CreateCommentRequest;
import com.georeport.entity.User;
import com.georeport.repository.UserRepository;
import com.georeport.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing comments on issues.
 */
@RestController
@RequestMapping("/api/issues/{issueId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Add a new comment to an issue
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommentResponse comment = commentService.addComment(issueId, request, user);

        return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .success(true)
                .message("Comment added successfully")
                .data(comment)
                .build());
    }

    /**
     * Get all comments for an issue (threaded)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long issueId) {

        List<CommentResponse> comments = commentService.getCommentsForIssue(issueId);

        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .success(true)
                .data(comments)
                .build());
    }

    /**
     * Get comment count for an issue
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCount(@PathVariable Long issueId) {
        long count = commentService.getCommentCount(issueId);

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .data(count)
                .build());
    }
}
