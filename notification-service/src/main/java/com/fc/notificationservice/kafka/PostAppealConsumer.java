package com.fc.notificationservice.kafka;

import com.fc.notification.AppealCreatedEvent;
import com.fc.notification.AppealReviewedEvent;
import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class PostAppealConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    // ------------------------------------------------------------
    // 1️⃣ Appeal Created → Notify Admin (STOMP only)
    // ------------------------------------------------------------
    @KafkaListener(
            topics = "appeals.created",
            groupId = "notification-service"
    )
    public void handleAppealCreated(ConsumerRecord<String, byte[]> record) {

        try {

            if (record.value() == null) {
                log.warn("Null payload received for appeals.created");
                return;
            }

            AppealCreatedEvent event =
                    AppealCreatedEvent.parseFrom(record.value());

            log.info("Appeal created | postId={}, userId={}",
                    event.getPostId(), event.getUserId());

            // Convert to JSON-friendly structure
            Map<String, Object> payload = Map.of(
                    "appealId", event.getAppealId(),
                    "postId", event.getPostId(),
                    "userId", event.getUserId(),
                    "reason", event.getReason(),
                    "createdAt", event.getCreatedAt()
            );

            messagingTemplate.convertAndSend(
                    "/topic/appeals.created",
                    payload
            );


        } catch (Exception e) {
            log.error("Failed to process AppealCreatedEvent", e);
        }
    }

    // ------------------------------------------------------------
    // 2️⃣ Appeal Reviewed → Notify User
    // ------------------------------------------------------------
    @KafkaListener(
            topics = "appeals.reviewed",
            groupId = "notification-service"
    )
    public void handleAppealReviewed(ConsumerRecord<String, byte[]> record) {

        try {

            if (record.value() == null) {
                log.warn("Null payload received for appeals.reviewed");
                return;
            }

            AppealReviewedEvent event =
                    AppealReviewedEvent.parseFrom(record.value());

            log.info("Appeal reviewed | appealId={}, status={}",
                    event.getAppealId(), event.getStatus());

            Notification notification = new Notification();
            notification.setSenderId(event.getReviewedBy());
            notification.setSenderName("Admin");
            notification.setSenderProfilePic(null);
            notification.setRecipientId(event.getUserId());

            if ("APPROVED".equalsIgnoreCase(event.getStatus())) {
                notification.setMessage("Your appeal was approved. Post restored.");
            } else {
                notification.setMessage("Your appeal was rejected.");
            }

            notification.setTimestamp(event.getReviewedAt());
            notification.setRead(false);

            notificationRepository.save(notification);

            messagingTemplate.convertAndSendToUser(
                    event.getUserId(),
                    "/queue/notifications",
                    notification
            );

            log.info("Appeal result notification sent to user={}",
                    event.getUserId());

        } catch (Exception e) {
            log.error("Failed to process AppealReviewedEvent", e);
        }
    }
}