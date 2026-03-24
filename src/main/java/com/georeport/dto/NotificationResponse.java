package com.georeport.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for notification response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long issueId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
