package com.georeport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new comment.
 */
@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;

    /**
     * Optional parent comment ID for replies
     */
    private Long parentId;
}
