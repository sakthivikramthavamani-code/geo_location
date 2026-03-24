package com.georeport.dto;

import com.georeport.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for updating issue status (admin action).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private IssueStatus status;

    private String notes;

    private Long assignedToId;
}
