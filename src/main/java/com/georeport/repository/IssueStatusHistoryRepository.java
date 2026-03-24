package com.georeport.repository;

import com.georeport.entity.IssueStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IssueStatusHistory entity operations.
 */
@Repository
public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {

    /**
     * Find all status history for an issue
     */
    List<IssueStatusHistory> findByIssueIdOrderByCreatedAtDesc(Long issueId);

    /**
     * Find latest status change for an issue
     */
    IssueStatusHistory findFirstByIssueIdOrderByCreatedAtDesc(Long issueId);
}
