package com.georeport.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for issue image response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueImageResponse {

    private Long id;
    private String fileName;
    private String originalName;
    private String imageUrl;
    private Long fileSize;
    private String contentType;
    private Boolean isPrimary;
    private LocalDateTime uploadedAt;
}
