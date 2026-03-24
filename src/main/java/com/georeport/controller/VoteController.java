package com.georeport.controller;

import com.georeport.dto.ApiResponse;
import com.georeport.entity.User;
import com.georeport.service.AuthService;
import com.georeport.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for vote/endorsement operations.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private AuthService authService;

    /**
     * Toggle vote on an issue
     * POST /api/issues/{id}/vote
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleVote(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        Map<String, Object> result = voteService.toggleVote(id, user);

        boolean hasVoted = (boolean) result.get("hasVoted");
        String message = hasVoted ? "Vote added" : "Vote removed";

        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    /**
     * Get vote status for current user
     * GET /api/issues/{id}/vote
     */
    @GetMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVoteStatus(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        Map<String, Object> result = voteService.getVoteStatus(id, user);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get vote count (public)
     * GET /api/issues/{id}/votes/count
     */
    @GetMapping("/{id}/votes/count")
    public ResponseEntity<ApiResponse<Long>> getVoteCount(@PathVariable Long id) {
        long count = voteService.getVoteCount(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
