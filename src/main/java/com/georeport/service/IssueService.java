package com.georeport.service;

import com.georeport.dto.*;
import com.georeport.entity.*;
import com.georeport.exception.BadRequestException;
import com.georeport.exception.ResourceNotFoundException;
import com.georeport.mapper.IssueMapper;
import com.georeport.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for issue management operations.
 * Handles CRUD operations, geo-queries, and status updates.
 */
@Service
public class IssueService {

    // Rough conversion: 1 degree latitude ≈ 111km
    private static final double METERS_PER_DEGREE = 111000.0;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private IssueImageRepository imageRepository;

    @Autowired
    private IssueStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IssueMapper issueMapper;

    @Autowired
    private DepartmentRoutingService departmentRoutingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new issue
     */
    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request, User reporter, List<MultipartFile> images) {
        // Get category
        IssueCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // --- SMART ROUTING: auto-assign department based on category ---
        String assignedDepartment = departmentRoutingService.route(category);

        // Create issue with lat/lng
        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .ward(request.getWard())
                .landmark(request.getLandmark())
                .category(category)
                .reporter(reporter)
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .status(IssueStatus.SUBMITTED)
                .priority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM)
                .department(assignedDepartment)
                .build();

        issue = issueRepository.save(issue);

        // Add status history
        IssueStatusHistory history = IssueStatusHistory.builder()
                .issue(issue)
                .newStatus(IssueStatus.SUBMITTED)
                .changedBy(reporter)
                .changeReason("Issue created")
                .build();
        statusHistoryRepository.save(history);

        // Handle image uploads
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (!file.isEmpty()) {
                    try {
                        String filename = fileStorageService.storeFile(file);
                        IssueImage image = IssueImage.builder()
                                .issue(issue)
                                .fileName(filename)
                                .originalName(file.getOriginalFilename())
                                .filePath(fileStorageService.getUploadPath().resolve(filename).toString())
                                .fileSize(file.getSize())
                                .contentType(file.getContentType())
                                .isPrimary(i == 0)
                                .build();
                        imageRepository.save(image);
                        issue.getImages().add(image);
                    } catch (IOException e) {
                        throw new BadRequestException("Failed to upload image: " + e.getMessage());
                    }
                }
            }
        }

        // Broadcast new issue to admin dashboard
        broadcastIssueUpdate("NEW_ISSUE", issue);

        return issueMapper.toResponse(issue);
    }

    /**
     * Get issue by ID
     */
    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));
        return issueMapper.toResponse(issue);
    }

    /**
     * Get all issues for a reporter
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesByReporter(Long reporterId) {
        return issueRepository.findByReporterIdOrderByCreatedAtDesc(reporterId)
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all issues with optional filters
     */
    @Transactional(readOnly = true)
    public Page<IssueResponse> getAllIssues(IssueStatus status, Long categoryId, String ward,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return issueRepository.findWithFilters(status, categoryId, ward, startDate, endDate, pageable)
                .map(issueMapper::toResponse);
    }

    /**
     * Get nearby issues
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getNearbyIssues(double latitude, double longitude, double radiusMeters) {
        // Convert meters to degrees (approximate)
        double radiusDegrees = radiusMeters / METERS_PER_DEGREE;

        return issueRepository.findNearbyIssues(latitude, longitude, radiusDegrees)
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get issues within bounding box (for map viewport)
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesInBounds(double minLng, double minLat, double maxLng, double maxLat) {
        return issueRepository.findIssuesInBoundingBox(minLng, minLat, maxLng, maxLat)
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Advanced search with dynamic filtering using Criteria API
     */
    @Transactional(readOnly = true)
    public Page<IssueResponse> searchIssues(IssueSearchRequest request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Issue> query = cb.createQuery(Issue.class);
        Root<Issue> root = query.from(Issue.class);

        List<Predicate> predicates = new ArrayList<>();

        // Keyword search (title or description)
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = "%" + request.getKeyword().toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), keyword);
            Predicate descMatch = cb.like(cb.lower(root.get("description")), keyword);
            predicates.add(cb.or(titleMatch, descMatch));
        }

        // Filter by statuses
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(request.getStatuses()));
        }

        // Filter by category IDs
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            predicates.add(root.get("category").get("id").in(request.getCategoryIds()));
        }

        // Filter by priorities
        if (request.getPriorities() != null && !request.getPriorities().isEmpty()) {
            predicates.add(root.get("priority").in(request.getPriorities()));
        }

        // Filter by ward
        if (request.getWard() != null && !request.getWard().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("ward"), request.getWard()));
        }

        // Date range filter
        if (request.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
        }

        // Radius/proximity filter
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadiusMeters() != null) {
            double radiusDegrees = request.getRadiusMeters() / METERS_PER_DEGREE;
            predicates.add(cb.between(root.get("latitude"),
                    request.getLatitude() - radiusDegrees,
                    request.getLatitude() + radiusDegrees));
            predicates.add(cb.between(root.get("longitude"),
                    request.getLongitude() - radiusDegrees,
                    request.getLongitude() + radiusDegrees));
        }

        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Sorting
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        if ("desc".equalsIgnoreCase(request.getSortDirection())) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }

        // Execute query with pagination
        TypedQuery<Issue> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Issue> results = typedQuery.getResultList();

        // Get total count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Issue> countRoot = countQuery.from(Issue.class);
        countQuery.select(cb.count(countRoot));

        // Rebuild predicates for count query (same filters)
        List<Predicate> countPredicates = new ArrayList<>();
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = "%" + request.getKeyword().toLowerCase() + "%";
            countPredicates.add(cb.or(
                    cb.like(cb.lower(countRoot.get("title")), keyword),
                    cb.like(cb.lower(countRoot.get("description")), keyword)));
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            countPredicates.add(countRoot.get("status").in(request.getStatuses()));
        }
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            countPredicates.add(countRoot.get("category").get("id").in(request.getCategoryIds()));
        }
        if (request.getPriorities() != null && !request.getPriorities().isEmpty()) {
            countPredicates.add(countRoot.get("priority").in(request.getPriorities()));
        }
        if (request.getWard() != null && !request.getWard().trim().isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("ward"), request.getWard()));
        }
        if (request.getStartDate() != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"), request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("createdAt"), request.getEndDate()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadiusMeters() != null) {
            double radiusDegrees = request.getRadiusMeters() / METERS_PER_DEGREE;
            countPredicates.add(cb.between(countRoot.get("latitude"),
                    request.getLatitude() - radiusDegrees, request.getLatitude() + radiusDegrees));
            countPredicates.add(cb.between(countRoot.get("longitude"),
                    request.getLongitude() - radiusDegrees, request.getLongitude() + radiusDegrees));
        }

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<IssueResponse> responseList = results.stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, total);
    }

    /**
     * Update issue status (admin action)
     */
    @Transactional
    public IssueResponse updateIssueStatus(Long issueId, UpdateStatusRequest request, User admin) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(request.getStatus());

        // Handle status-specific updates
        if (request.getStatus() == IssueStatus.RESOLVED) {
            issue.setResolvedAt(LocalDateTime.now());
            issue.setResolutionNotes(request.getNotes());
        } else if (request.getStatus() == IssueStatus.REJECTED) {
            issue.setRejectionReason(request.getNotes());
        }

        // Assign to admin if provided
        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToId()));
            issue.setAssignedTo(assignee);
        }

        issue = issueRepository.save(issue);

        // Add status history
        IssueStatusHistory history = IssueStatusHistory.builder()
                .issue(issue)
                .oldStatus(oldStatus)
                .newStatus(request.getStatus())
                .changedBy(admin)
                .changeReason(request.getNotes())
                .build();
        statusHistoryRepository.save(history);

        // Create notification for reporter
        createNotification(issue.getReporter(), issue,
                "Issue Status Updated",
                String.format("Your issue '%s' status changed to %s", issue.getTitle(),
                        request.getStatus().getDisplayName()),
                "STATUS_UPDATE");

        // Broadcast update via WebSocket
        broadcastIssueUpdate("STATUS_UPDATE", issue);

        // Send personal notification to reporter
        sendPersonalNotification(issue.getReporter().getId(), issue);

        return issueMapper.toResponse(issue);
    }

    /**
     * Get status history for an issue
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getStatusHistory(Long issueId) {
        return statusHistoryRepository.findByIssueIdOrderByCreatedAtDesc(issueId)
                .stream()
                .map(issueMapper::toStatusHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long total = issueRepository.count();
        long submitted = issueRepository.countByStatus(IssueStatus.SUBMITTED);
        long inProgress = issueRepository.countByStatus(IssueStatus.IN_PROGRESS);
        long resolved = issueRepository.countByStatus(IssueStatus.RESOLVED);
        long rejected = issueRepository.countByStatus(IssueStatus.REJECTED);

        Map<String, Long> byCategory = new HashMap<>();
        issueRepository.getIssueCountByCategory().forEach(row -> byCategory.put((String) row[0], (Long) row[1]));

        Map<String, Long> byWard = new HashMap<>();
        issueRepository.findAllDistinctWards().forEach(ward -> byWard.put(ward, issueRepository.countByWard(ward)));

        return DashboardStats.builder()
                .totalIssues(total)
                .submittedCount(submitted)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .issuesByCategory(byCategory)
                .issuesByWard(byWard)
                .build();
    }

    /**
     * Get all categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByPriorityAsc()
                .stream()
                .map(issueMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get issues filtered by department (for department dashboards).
     * Each department sees ONLY their own complaints.
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesByDepartment(String department) {
        return issueRepository.findByDepartmentOrderByCreatedAtDesc(department)
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get per-department stats summary.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentStats() {
        Map<String, Long> stats = new HashMap<>();
        issueRepository.getIssueCountByDepartment()
                .forEach(row -> stats.put((String) row[0], (Long) row[1]));
        return stats;
    }

    /**
     * Delete issue (Admin or Reporter only)
     */
    @Transactional
    public void deleteIssue(Long issueId, User user) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        // Check permission
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleType.ROLE_ADMIN);

        if (!isAdmin && !issue.getReporter().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to delete this issue");
        }

        // Delete associated files
        if (issue.getImages() != null) {
            for (IssueImage img : issue.getImages()) {
                fileStorageService.deleteFile(img.getFileName());
            }
        }

        issueRepository.delete(issue);
    }

    /**
     * Update issue details (Reporter only)
     */
    @Transactional
    public IssueResponse updateIssueDetails(Long issueId, CreateIssueRequest request, User user) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        // Check permission: Only reporter can edit details
        if (!issue.getReporter().getId().equals(user.getId())) {
            throw new BadRequestException("Only the reporter can edit this issue");
        }

        // Allow edits only if status is SUBMITTED or IN_PROGRESS
        if (issue.getStatus() != IssueStatus.SUBMITTED && issue.getStatus() != IssueStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot edit issue in " + issue.getStatus() + " status");
        }

        // Update fields
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setAddress(request.getAddress());
        issue.setWard(request.getWard());
        issue.setLandmark(request.getLandmark());
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }

        // Update category if changed
        if (!issue.getCategory().getId().equals(request.getCategoryId())) {
            IssueCategory newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            issue.setCategory(newCategory);
        }

        issue = issueRepository.save(issue);
        return issueMapper.toResponse(issue);
    }

    /**
     * Create notification for a user
     */
    private void createNotification(User user, Issue issue, String title, String message, String type) {
        Notification notification = Notification.builder()
                .user(user)
                .issue(issue)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Broadcast issue update via WebSocket
     */
    private void broadcastIssueUpdate(String eventType, Issue issue) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("issue", issueMapper.toResponse(issue));
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/issues", payload);
    }

    /**
     * Send personal notification via WebSocket
     */
    private void sendPersonalNotification(Long userId, Issue issue) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "NOTIFICATION");
        payload.put("issueId", issue.getId());
        payload.put("title", issue.getTitle());
        payload.put("status", issue.getStatus().name());
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload);
    }
}
