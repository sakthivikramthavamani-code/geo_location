package com.georeport.service;

import com.georeport.dto.NotificationResponse;
import com.georeport.entity.Issue;
import com.georeport.entity.Notification;
import com.georeport.entity.User;
import com.georeport.mapper.IssueMapper;
import com.georeport.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for notification operations.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private IssueMapper issueMapper;

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(issueMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(issueMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Create a new notification for a user
     */
    @Transactional
    public Notification createNotification(User user, String title, String message, String type, Long issueId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        // Set issue if provided
        if (issueId != null) {
            // We need to set the issue, but we don't want to inject IssueRepository here
            // Instead, we'll use a reference
            Issue issue = new Issue();
            issue.setId(issueId);
            notification.setIssue(issue);
        }

        return notificationRepository.save(notification);
    }
}
