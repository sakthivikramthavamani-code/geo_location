package com.georeport.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for comment data with nested replies.
 */
@Data
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private Long issueId;

    // Author info
    private Long authorId;
    private String authorName;
    private Boolean isAdminReply;

    // Parent reference for threading
    private Long parentId;

    // Nested replies
    private List<CommentResponse> replies;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
