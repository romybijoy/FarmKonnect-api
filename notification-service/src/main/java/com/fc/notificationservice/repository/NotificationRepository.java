package com.fc.notificationservice.repository;

import com.fc.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing Notification documents in MongoDB.
 * Provides convenience methods to:
 * - Fetch all notifications for a user (sorted by newest first)
 * - Fetch unread notifications for a user
 */
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Retrieves all notifications for a given user, ordered by timestamp DESC.
     *
     * @param recipientId ID of the user receiving the notifications
     * @return list of notifications (newest first)
     */
    List<Notification> findByRecipientIdOrderByTimestampDesc(String recipientId);

    /**
     * Retrieves only unread notifications for a given user.
     *
     * @param recipientId ID of the user
     * @return list of unread notifications
     */
    List<Notification> findByRecipientIdAndReadFalse(String recipientId); // unread only
}