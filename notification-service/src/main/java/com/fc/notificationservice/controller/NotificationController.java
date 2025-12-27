package com.fc.notificationservice.controller;


import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing notification retrieval and read status updates.
 * Provides APIs for:
 * - Fetching notifications for a user
 * - Marking a single notification as read
 * - Marking all notifications for a user as read
 */
@Slf4j
@RestController
@Tag(name = "Notification API", description = "Endpoints for managing user notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Retrieves all notifications for a given user, ordered by newest first.
     *
     * @param userId recipient user ID
     * @return list of notifications
     */
    @Operation(summary = "Get all notifications for a user")
    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable String userId) {
        log.debug("Fetching notifications for userId={}", userId);
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(userId);
    }

    /**
     * Marks a specific notification as read.
     *
     * @param id notification ID
     * @return updated notification
     */
    @Operation(summary = "Mark a notification as read")
    @PatchMapping("/read/{id}")
    public Notification markAsRead(@PathVariable String id) {
        log.debug("Marking notification as read: id={}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Marks all unread notifications for a user as read.
     *
     * @param userId recipient user ID
     * @return list of updated notifications
     */
    @Operation(summary = "Mark all notifications for a user as read")
    @PatchMapping("/read-all/{userId}")
    public List<Notification> markAllAsRead(@PathVariable String userId) {
        log.debug("Marking all notifications as read for userId={}", userId);

        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalse(userId);

        notifications.forEach(n -> n.setRead(true));

        return notificationRepository.saveAll(notifications);
    }
}
