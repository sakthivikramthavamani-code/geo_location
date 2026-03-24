package com.georeport.repository;

import com.georeport.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Vote entity operations.
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * Find a vote by user and issue
     */
    Optional<Vote> findByUserIdAndIssueId(Long userId, Long issueId);

    /**
     * Check if user has voted
     */
    boolean existsByUserIdAndIssueId(Long userId, Long issueId);

    /**
     * Count votes for an issue
     */
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.issue.id = :issueId")
    long countByIssueId(@Param("issueId") Long issueId);

    /**
     * Delete vote by user and issue
     */
    void deleteByUserIdAndIssueId(Long userId, Long issueId);
}
