package com.georeport.repository;

import com.georeport.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all top-level comments (no parent) for an issue, ordered by creation
     * time
     */
    @Query("SELECT c FROM Comment c WHERE c.issue.id = :issueId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByIssueId(@Param("issueId") Long issueId);

    /**
     * Find all comments for an issue (including replies)
     */
    List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);

    /**
     * Count comments for an issue
     */
    long countByIssueId(Long issueId);

    /**
     * Find replies to a specific comment
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
