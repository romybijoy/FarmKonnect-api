package com.fc.notificationservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a notification sent to a user.
 * Notifications are stored in the "notifications" collection and include:
 * - sender details
 * - recipient user ID
 * - human-readable message
 * - timestamp of the event
 * - read/unread status
 */
@Data
@Document(collection = "notifications")
public class Notification {
    /** Unique MongoDB document identifier */
    @Id
    private String id;

    /** ID of the user who triggered the notification (e.g., follower) */
    private String senderId;

    /** ID of the user receiving the notification */
    private String recipientId;

    /** Display name of the sender */
    private String senderName;

    /** Sender's profile picture URL */
    private String senderProfilePic;

    /** Notification message text */
    private String message;

    /** Unix timestamp (milliseconds) when the event occurred */
    private long timestamp;

    /** Whether the notification has been read by the recipient */
    private boolean read = false;
}
