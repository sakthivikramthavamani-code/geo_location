package com.georeport.repository;

import com.georeport.entity.IssueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for IssueCategory entity operations.
 */
@Repository
public interface IssueCategoryRepository extends JpaRepository<IssueCategory, Long> {

    /**
     * Find category by name
     */
    Optional<IssueCategory> findByName(String name);

    /**
     * Find all active categories ordered by priority
     */
    List<IssueCategory> findByIsActiveTrueOrderByPriorityAsc();

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);
}
