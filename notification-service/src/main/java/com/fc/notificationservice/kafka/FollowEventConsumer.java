package com.fc.notificationservice.kafka;

import com.fc.notification.FollowEvent;
import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for handling FollowEvent messages.
 * Once a follow event is consumed:
 *  1. Convert Protobuf → FollowEvent model
 *  2. Persist notification in MongoDB
 *  3. Broadcast notification via WebSocket to the recipient
 */
@Slf4j
@Service
public class FollowEventConsumer {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Consumes follow event messages from Kafka, converts them into Notification objects,
     * saves them in the database, and sends real-time WebSocket notifications to the user.
     *
     * @param messageRecord Kafka record containing a serialized FollowEvent protobuf message
     */
    @KafkaListener(topics = "follow-events", groupId = "notification-service")
    public void handleFollowEvent(ConsumerRecord<String, byte[]> messageRecord) {
        try {
            byte[] data = messageRecord.value();

            // Parse Protobuf message into Java object
            FollowEvent event = FollowEvent.parseFrom(data);

            // Build and save notification
            Notification notification = new Notification();
            notification.setSenderId(event.getSenderId());
            notification.setRecipientId(event.getRecipientId());
            notification.setSenderName(event.getSenderName());
            notification.setSenderProfilePic(event.getSenderProfilePic());
            notification.setMessage(event.getSenderName() + " followed you.");
            notification.setTimestamp(event.getTimestamp());
            notification.setRead(false);

            // Save to MongoDB
            notificationRepository.save(notification);

            // Broadcast notification to WebSocket user destination
            messagingTemplate.convertAndSendToUser(
                    event.getRecipientId(),                 // user ID
                    "/queue/notifications",                 // destination
                    notification                             // payload
            );

            log.info("Notification sent to recipientId={}", event.getRecipientId());

        } catch (Exception e) {
            log.error("Failed to process FollowEvent: {}", e.getMessage(), e);
        }
    }

}