package com.georeport.service;

import com.georeport.dto.AnalyticsResponse;
import com.georeport.entity.Issue;
import com.georeport.entity.IssueStatus;
import com.georeport.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating analytics data for the admin dashboard.
 */
@Service
public class AnalyticsService {

        @Autowired
        private IssueRepository issueRepository;

        /**
         * Get comprehensive analytics data
         */
        @Transactional(readOnly = true)
        public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
                // If no date range provided, use last 30 days as default for detailed metrics
                LocalDateTime actualStart = (startDate != null) ? startDate : LocalDateTime.now().minusDays(30);
                LocalDateTime actualEnd = (endDate != null) ? endDate : LocalDateTime.now();

                // Fetch metrics using optimized repository queries instead of fetching ALL
                // issues
                List<Object[]> statusCounts = issueRepository.getIssueCountByStatusSince(actualStart);
                Map<String, Long> byStatus = new HashMap<>();
                for (Object[] row : statusCounts) {
                        byStatus.put(((IssueStatus) row[0]).name(), (Long) row[1]);
                }

                List<Issue> filteredIssues = issueRepository.findWithFilters(null, null, null, actualStart, actualEnd,
                                PageRequest.of(0, 1000, Sort.by("createdAt").descending())).getContent();

                return AnalyticsResponse.builder()
                                .byStatus(byStatus)
                                .byCategory(getCountByCategory(filteredIssues))
                                .byPriority(getCountByPriority(filteredIssues))
                                .byWard(getCountByWard(filteredIssues))
                                .issueTrends(getIssueTrends(filteredIssues))
                                .avgResolutionTimeByCategory(getAvgResolutionTimeByCategory(filteredIssues))
                                .hotspots(getHotspots(filteredIssues))
                                .overall(getOverallStatsOptimized(actualStart)) // Use optimized stats
                                .build();
        }

        /**
         * Get issue trends (issues per day for last 30 days)
         */
        @Transactional(readOnly = true)
        public Map<String, Long> getTrends(int days) {
                LocalDateTime startDate = LocalDateTime.now().minusDays(days);
                List<Issue> issues = issueRepository.findAll().stream()
                                .filter(i -> i.getCreatedAt() != null && i.getCreatedAt().isAfter(startDate))
                                .collect(Collectors.toList());

                return getIssueTrends(issues);
        }

        /**
         * Get category distribution
         */
        @Transactional(readOnly = true)
        public Map<String, Long> getCategoryDistribution() {
                return getCountByCategory(issueRepository.findAll());
        }

        /**
         * Get average resolution time by category
         */
        @Transactional(readOnly = true)
        public Map<String, Double> getResolutionTimes() {
                return getAvgResolutionTimeByCategory(issueRepository.findAll());
        }

        /**
         * Get hotspot data for heatmap
         */
        @Transactional(readOnly = true)
        public List<AnalyticsResponse.HotspotData> getHotspotData() {
                return getHotspots(issueRepository.findAll());
        }

        // Helper methods

        private Map<String, Long> getCountByStatus(List<Issue> issues) {
                return issues.stream()
                                .filter(i -> i.getStatus() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getStatus().name(),
                                                Collectors.counting()));
        }

        private Map<String, Long> getCountByCategory(List<Issue> issues) {
                return issues.stream()
                                .filter(i -> i.getCategory() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getCategory().getName(),
                                                Collectors.counting()));
        }

        private Map<String, Long> getCountByPriority(List<Issue> issues) {
                return issues.stream()
                                .filter(i -> i.getPriority() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getPriority().name(),
                                                Collectors.counting()));
        }

        private Map<String, Long> getCountByWard(List<Issue> issues) {
                return issues.stream()
                                .filter(i -> i.getWard() != null && !i.getWard().trim().isEmpty())
                                .collect(Collectors.groupingBy(
                                                Issue::getWard,
                                                Collectors.counting()));
        }

        private Map<String, Long> getIssueTrends(List<Issue> issues) {
                Map<String, Long> trends = new LinkedHashMap<>();

                // Get last 30 days
                LocalDate today = LocalDate.now();
                for (int i = 29; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        trends.put(date.toString(), 0L);
                }

                // Count issues per day
                issues.stream()
                                .filter(i -> i.getCreatedAt() != null)
                                .forEach(issue -> {
                                        String dateKey = issue.getCreatedAt().toLocalDate().toString();
                                        if (trends.containsKey(dateKey)) {
                                                trends.put(dateKey, trends.get(dateKey) + 1);
                                        }
                                });

                return trends;
        }

        private Map<String, Double> getAvgResolutionTimeByCategory(List<Issue> issues) {
                Map<String, List<Double>> resolutionTimes = new HashMap<>();

                issues.stream()
                                .filter(i -> i.getStatus() == IssueStatus.RESOLVED &&
                                                i.getCreatedAt() != null &&
                                                i.getResolvedAt() != null &&
                                                i.getCategory() != null)
                                .forEach(issue -> {
                                        Duration duration = Duration.between(issue.getCreatedAt(),
                                                        issue.getResolvedAt());
                                        double hours = duration.toHours();
                                        String category = issue.getCategory().getName();

                                        resolutionTimes.computeIfAbsent(category, k -> new ArrayList<>()).add(hours);
                                });

                return resolutionTimes.entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> e.getValue().stream().mapToDouble(Double::doubleValue).average()
                                                                .orElse(0.0)));
        }

        private List<AnalyticsResponse.HotspotData> getHotspots(List<Issue> issues) {
                // Group issues by approximate location (grid-based clustering)
                Map<String, List<Issue>> locationGroups = new HashMap<>();
                double gridSize = 0.01; // ~1km grid

                issues.stream()
                                .filter(i -> i.getLatitude() != null && i.getLongitude() != null)
                                .forEach(issue -> {
                                        double gridLat = Math.round(issue.getLatitude() / gridSize) * gridSize;
                                        double gridLng = Math.round(issue.getLongitude() / gridSize) * gridSize;
                                        String key = gridLat + "," + gridLng;
                                        locationGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(issue);
                                });

                return locationGroups.entrySet().stream()
                                .map(entry -> {
                                        String[] coords = entry.getKey().split(",");
                                        return AnalyticsResponse.HotspotData.builder()
                                                        .latitude(Double.parseDouble(coords[0]))
                                                        .longitude(Double.parseDouble(coords[1]))
                                                        .count((long) entry.getValue().size())
                                                        .build();
                                })
                                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                                .limit(100) // Top 100 hotspots
                                .collect(Collectors.toList());
        }

        private AnalyticsResponse.OverallStats getOverallStatsOptimized(LocalDateTime since) {
                long total = issueRepository.count();

                LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
                LocalDateTime weekStart = LocalDateTime.now().minusDays(7);

                long resolvedThisMonth = issueRepository.countByStatusAndResolvedAtBetween(IssueStatus.RESOLVED,
                                monthStart, LocalDateTime.now());
                long newThisWeek = issueRepository.countByCreatedAtBetween(weekStart, LocalDateTime.now());

                Double avgResolutionHours = issueRepository
                                .getAverageResolutionTimeSince(LocalDateTime.now().minusMonths(6));

                long resolved = issueRepository.countByStatus(IssueStatus.RESOLVED);
                double resolutionRate = total > 0 ? (resolved * 100.0 / total) : 0.0;

                return AnalyticsResponse.OverallStats.builder()
                                .totalIssues(total)
                                .resolvedThisMonth(resolvedThisMonth)
                                .newThisWeek(newThisWeek)
                                .avgResolutionTimeHours(avgResolutionHours != null
                                                ? Math.round(avgResolutionHours * 10.0) / 10.0
                                                : 0.0)
                                .resolutionRate(Math.round(resolutionRate * 10.0) / 10.0)
                                .build();
        }
}
