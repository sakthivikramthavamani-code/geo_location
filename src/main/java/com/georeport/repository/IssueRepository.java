package com.georeport.repository;

import com.georeport.entity.Issue;
import com.georeport.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Issue entity operations.
 * Uses simple math for distance calculations (H2 compatible).
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

        /**
         * Find all issues by reporter
         */
        List<Issue> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

        /**
         * Find all issues by status
         */
        List<Issue> findByStatusOrderByCreatedAtDesc(IssueStatus status);

        /**
         * Find issues by category
         */
        List<Issue> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

        /**
         * Find issues by department (used by department dashboards)
         */
        List<Issue> findByDepartmentOrderByCreatedAtDesc(String department);

        /**
         * Count issues by department
         */
        long countByDepartment(String department);

        /**
         * Find issues by ward
         */
        List<Issue> findByWardOrderByCreatedAtDesc(String ward);

        /**
         * Find issues assigned to a specific admin
         */
        List<Issue> findByAssignedToIdOrderByCreatedAtDesc(Long adminId);

        /**
         * Find issues within a date range
         */
        @Query("SELECT i FROM Issue i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
        List<Issue> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find nearby issues using Haversine formula approximation.
         * Calculates distance in degrees (roughly) for simplicity.
         */
        @Query("SELECT i FROM Issue i WHERE " +
                        "ABS(i.latitude - :latitude) < :radiusDegrees AND " +
                        "ABS(i.longitude - :longitude) < :radiusDegrees " +
                        "ORDER BY (i.latitude - :latitude) * (i.latitude - :latitude) + " +
                        "(i.longitude - :longitude) * (i.longitude - :longitude)")
        List<Issue> findNearbyIssues(@Param("latitude") double latitude,
                        @Param("longitude") double longitude,
                        @Param("radiusDegrees") double radiusDegrees);

        /**
         * Find issues within a bounding box (for map viewport).
         */
        @Query("SELECT i FROM Issue i WHERE " +
                        "i.longitude >= :minLng AND i.longitude <= :maxLng AND " +
                        "i.latitude >= :minLat AND i.latitude <= :maxLat " +
                        "ORDER BY i.createdAt DESC")
        List<Issue> findIssuesInBoundingBox(@Param("minLng") double minLng,
                        @Param("minLat") double minLat,
                        @Param("maxLng") double maxLng,
                        @Param("maxLat") double maxLat);

        /**
         * Count issues by status
         */
        long countByStatus(IssueStatus status);

        /**
         * Count issues by category
         */
        long countByCategoryId(Long categoryId);

        /**
         * Count issues by ward
         */
        long countByWard(String ward);

        /**
         * Get all distinct wards with issues
         */
        @Query("SELECT DISTINCT i.ward FROM Issue i WHERE i.ward IS NOT NULL ORDER BY i.ward")
        List<String> findAllDistinctWards();

        /**
         * Find issues with pagination and multiple filters
         */
        @Query("SELECT i FROM Issue i WHERE " +
                        "(:status IS NULL OR i.status = :status) AND " +
                        "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
                        "(:ward IS NULL OR i.ward = :ward) AND " +
                        "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
                        "(:endDate IS NULL OR i.createdAt <= :endDate)")
        Page<Issue> findWithFilters(@Param("status") IssueStatus status,
                        @Param("categoryId") Long categoryId,
                        @Param("ward") String ward,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Get issue statistics by status
         */
        @Query("SELECT i.status, COUNT(i) FROM Issue i GROUP BY i.status")
        List<Object[]> getIssueCountByStatus();

        /**
         * Get issue statistics by category
         */
        @Query("SELECT i.category.name, COUNT(i) FROM Issue i GROUP BY i.category.name")
        List<Object[]> getIssueCountByCategory();

        /**
         * Get issue count per department (for routing stats)
         */
        @Query("SELECT i.department, COUNT(i) FROM Issue i WHERE i.department IS NOT NULL GROUP BY i.department")
        List<Object[]> getIssueCountByDepartment();


        /**
         * Count issues reported within a date range
         */
        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        /**
         * Count resolved issues within a date range
         */
        long countByStatusAndResolvedAtBetween(IssueStatus status, LocalDateTime start, LocalDateTime end);

        /**
         * Get issue count by status for issues created after a certain date
         */
        @Query("SELECT i.status, COUNT(i) FROM Issue i WHERE i.createdAt >= :since GROUP BY i.status")
        List<Object[]> getIssueCountByStatusSince(@Param("since") LocalDateTime since);

        /**
         * Get average resolution time in hours since a certain date
         */
        @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, i.createdAt, i.resolvedAt)) FROM Issue i " +
                        "WHERE i.status = 'RESOLVED' AND i.createdAt >= :since")
        Double getAverageResolutionTimeSince(@Param("since") LocalDateTime since);
}
