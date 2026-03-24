package com.georeport.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for issue status history response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryResponse {

    private Long id;
    private String oldStatus;
    private String newStatus;
    private String changedByName;
    private String changeReason;
    private LocalDateTime createdAt;
}
