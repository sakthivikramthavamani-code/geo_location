package com.georeport.mapper;

import com.georeport.dto.*;
import com.georeport.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between Issue entities and DTOs.
 */
@Component
public class IssueMapper {

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * Convert Issue entity to IssueResponse DTO
     */
    public IssueResponse toResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .latitude(issue.getLatitude())
                .longitude(issue.getLongitude())
                .address(issue.getAddress())
                .ward(issue.getWard())
                .landmark(issue.getLandmark())
                .status(issue.getStatus().name())
                .statusDisplayName(issue.getStatus().getDisplayName())
                .priority(issue.getPriority().name())
                .department(issue.getDepartment())
                .categoryId(issue.getCategory().getId())
                .categoryName(issue.getCategory().getName())
                .categoryIcon(issue.getCategory().getIcon())
                .categoryColor(issue.getCategory().getColor())
                .reporterId(issue.getReporter().getId())
                .reporterName(issue.getReporter().getFullName())
                .reporterEmail(issue.getReporter().getEmail())
                .contactEmail(issue.getContactEmail())
                .contactPhone(issue.getContactPhone())
                .assignedToId(issue.getAssignedTo() != null ? issue.getAssignedTo().getId() : null)
                .assignedToName(issue.getAssignedTo() != null ? issue.getAssignedTo().getFullName() : null)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .resolvedAt(issue.getResolvedAt())
                .resolutionNotes(issue.getResolutionNotes())
                .rejectionReason(issue.getRejectionReason())
                .images(issue.getImages() != null ? issue.getImages().stream()
                        .map(this::toImageResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Convert IssueImage entity to IssueImageResponse DTO
     */
    public IssueImageResponse toImageResponse(IssueImage image) {
        if (image == null) {
            return null;
        }

        return IssueImageResponse.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .originalName(image.getOriginalName())
                .imageUrl("/uploads/" + image.getFileName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .isPrimary(image.getIsPrimary())
                .uploadedAt(image.getUploadedAt())
                .build();
    }

    /**
     * Convert IssueCategory entity to CategoryResponse DTO
     */
    public CategoryResponse toCategoryResponse(IssueCategory category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .priority(category.getPriority())
                .build();
        
        // Set isActive using setter (Lombok @Data generates setIsActive)
        response.setIsActive(category.getIsActive());
        
        return response;
    }

    /**
     * Convert IssueStatusHistory entity to StatusHistoryResponse DTO
     */
    public StatusHistoryResponse toStatusHistoryResponse(IssueStatusHistory history) {
        if (history == null) {
            return null;
        }

        return StatusHistoryResponse.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus() != null ? history.getOldStatus().name() : null)
                .newStatus(history.getNewStatus().name())
                .changedByName(history.getChangedBy() != null ? history.getChangedBy().getFullName() : "System")
                .changeReason(history.getChangeReason())
                .createdAt(history.getCreatedAt())
                .build();
    }

    /**
     * Convert Notification entity to NotificationResponse DTO
     */
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .issueId(notification.getIssue() != null ? notification.getIssue().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
