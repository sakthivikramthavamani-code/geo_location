package com.georeport.repository;

import com.georeport.entity.IssueImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IssueImage entity operations.
 */
@Repository
public interface IssueImageRepository extends JpaRepository<IssueImage, Long> {

    /**
     * Find all images for an issue
     */
    List<IssueImage> findByIssueIdOrderByUploadedAtDesc(Long issueId);

    /**
     * Find primary image for an issue
     */
    IssueImage findByIssueIdAndIsPrimaryTrue(Long issueId);

    /**
     * Delete all images for an issue
     */
    void deleteByIssueId(Long issueId);
}
