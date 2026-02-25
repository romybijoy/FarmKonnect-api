package com.fc.notificationservice.kafka;

import com.fc.notification.PostModerationEvent;
import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Post Moderation Consumer
 *
 * @author Romyb
 * @since 19/02/2026
 */
@Slf4j
@Service
public class PostModerationConsumer {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "posts.moderated", groupId = "notification-service")
    public void handlePostModeration(ConsumerRecord<String, byte[]> record) {
        log.info("RAW MESSAGE RECEIVED");
        try {
            PostModerationEvent event =
                    PostModerationEvent.parseFrom(record.value());

            log.info("Received moderation event | postId={}, ownerId={}",
                    event.getPostId(), event.getOwnerId());

            Notification notification = new Notification();
            notification.setSenderId(event.getModeratedBy());
            notification.setSenderName("Admin");
            notification.setSenderProfilePic(null);

            notification.setRecipientId(event.getOwnerId());
            notification.setTimestamp(event.getTimestamp());
            notification.setRead(false);

            // Build message based on action
            String message;
            if ("REMOVED".equalsIgnoreCase(event.getAction())) {
                message = event.getReason();
                if (message.isBlank()) {
                    message = "Your post was removed.";
                }
            } else if ("RESTORED".equalsIgnoreCase(event.getAction())) {
                message = "Your post has been restored by admin.";
            } else {
                message = "Your post moderation status changed.";
            }

            notification.setMessage(message);

            notificationRepository.save(notification);

            messagingTemplate.convertAndSendToUser(
                    event.getOwnerId(),
                    "/queue/notifications",
                    notification
            );

            log.info("Moderation notification sent to user={}", event.getOwnerId());

        } catch (Exception e) {
            log.error("Failed to process PostModerationEvent", e);
        }
    }
}

