package com.georeport.service;

import com.georeport.dto.AnalyticsResponse;
import com.georeport.entity.Issue;
import com.georeport.entity.IssueStatus;
import com.georeport.repository.IssueRepository;
import com.georeport.repository.IssueCategoryRepository;
import com.georeport.repository.RoleRepository;
import com.georeport.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGetAnalyticsOptimized() {
        // Create mandatory dependencies
        com.georeport.entity.IssueCategory category = categoryRepository.findAll().stream().findFirst().orElse(null);
        com.georeport.entity.User user = userRepository.findAll().stream().findFirst().orElse(null);

        if (category == null || user == null) {
            fail("Required test data (category or user) not found. Ensure test database is initialized.");
            return;
        }

        final com.georeport.entity.IssueCategory finalCategory = category;
        final com.georeport.entity.User finalUser = user;

        // Create some test data
        Issue issue1 = Issue.builder()
                .title("Test Issue 1")
                .description("Test Description 1")
                .latitude(10.0)
                .longitude(20.0)
                .category(finalCategory)
                .reporter(finalUser)
                .status(IssueStatus.RESOLVED)
                .createdAt(LocalDateTime.now().minusDays(10))
                .resolvedAt(LocalDateTime.now().minusDays(5))
                .build();

        Issue issue2 = Issue.builder()
                .title("Test Issue 2")
                .description("Test Description 2")
                .latitude(11.0)
                .longitude(21.0)
                .category(finalCategory)
                .reporter(finalUser)
                .status(IssueStatus.SUBMITTED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        issueRepository.save(issue1);
        issueRepository.save(issue2);

        // Run analytics for last 30 days
        AnalyticsResponse response = analyticsService.getAnalytics(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now());

        assertNotNull(response);
        assertNotNull(response.getOverall());
        assertTrue(response.getOverall().getTotalIssues() >= 2);
    }
}
