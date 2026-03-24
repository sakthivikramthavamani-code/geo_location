package com.georeport.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an image attached to an issue.
 * Stores metadata for uploaded image files.
 */
@Entity
@Table(name = "issue_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    /**
     * Get the URL path for accessing this image
     */
    public String getImageUrl() {
        return "/uploads/" + fileName;
    }
}
