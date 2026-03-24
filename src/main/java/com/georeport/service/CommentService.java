package com.georeport.service;

import com.georeport.dto.CommentResponse;
import com.georeport.dto.CreateCommentRequest;
import com.georeport.entity.Comment;
import com.georeport.entity.Issue;
import com.georeport.entity.User;
import com.georeport.exception.ResourceNotFoundException;
import com.georeport.repository.CommentRepository;
import com.georeport.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing comments on issues.
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    /**
     * Add a new comment to an issue
     */
    @Transactional
    public CommentResponse addComment(Long issueId, CreateCommentRequest request, User author) {
        Objects.requireNonNull(issueId, "issueId must not be null");
        @SuppressWarnings("null")
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .issue(issue)
                .author(author)
                .isAdminReply(author.isAdmin())
                .build();

        // Handle reply to parent comment
        if (request.getParentId() != null) {
            Long parentId = request.getParentId();
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        @SuppressWarnings("null")
        Comment saved = commentRepository.save(comment);
        if (saved == null) {
            throw new IllegalStateException("Saved comment is null");
        }

        // Notify issue reporter if admin commented
        if (author.isAdmin() && issue.getReporter() != null && !issue.getReporter().getId().equals(author.getId())) {
            notificationService.createNotification(
                    issue.getReporter(),
                    "New Response",
                    "Admin responded to your issue: " + issue.getTitle(),
                    "COMMENT",
                    issue.getId());

            // WebSocket notification
            if (issue.getReporter().getId() != null) {
                @SuppressWarnings("null")
                Object payload = buildCommentNotification(saved, issue);
                messagingTemplate.convertAndSend(
                        "/topic/notifications/" + issue.getReporter().getId(),
                        payload);
            }
        }

        // Notify admin if citizen added comment
        if (!author.isAdmin()) {
            @SuppressWarnings("null")
            Object payload = buildCommentNotification(saved, issue);
            messagingTemplate.convertAndSend(
                    "/topic/issues",
                    payload);
        }

        return toResponse(saved);
    }

    /**
     * Get all comments for an issue (threaded structure)
     */
    public List<CommentResponse> getCommentsForIssue(Long issueId) {
        // Get only top-level comments, replies are nested
        List<Comment> topLevelComments = commentRepository.findTopLevelCommentsByIssueId(issueId);
        return topLevelComments.stream()
                .map(this::toResponseWithReplies)
                .collect(Collectors.toList());
    }

    /**
     * Count comments for an issue
     */
    public long getCommentCount(Long issueId) {
        return commentRepository.countByIssueId(issueId);
    }

    /**
     * Convert Comment entity to response DTO (without nested replies)
     */
    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .issueId(comment.getIssue().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getFullName())
                .isAdminReply(comment.getIsAdminReply())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Convert Comment entity to response DTO with nested replies
     */
    private CommentResponse toResponseWithReplies(Comment comment) {
        CommentResponse response = toResponse(comment);

        // Recursively load replies
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            response.setReplies(
                    comment.getReplies().stream()
                            .map(this::toResponseWithReplies)
                            .collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * Build WebSocket notification payload for new comment
     */
    private Object buildCommentNotification(Comment comment, Issue issue) {
        return java.util.Map.of(
                "eventType", "NEW_COMMENT",
                "issueId", issue.getId(),
                "issueTitle", issue.getTitle(),
                "commentId", comment.getId(),
                "authorName", comment.getAuthor().getFullName(),
                "isAdminReply", comment.getIsAdminReply(),
                "preview", comment.getContent().substring(0, Math.min(100, comment.getContent().length())));
    }
}
