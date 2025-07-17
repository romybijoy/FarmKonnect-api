package com.fc.notificationservice.kafka;


import com.fc.notification.FollowEvent;
import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FollowEventConsumer {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "follow-events", groupId = "notification-service")
    public void handleFollowEvent(ConsumerRecord<String, byte[]> record) {
        try {
            byte[] data = record.value();

            // Parse Protobuf message
            FollowEvent event = FollowEvent.parseFrom(data);

            // Build notification document
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

            // Send to WebSocket user destination (recipient ID used as username)
            messagingTemplate.convertAndSendToUser(
                    event.getRecipientId(),                 // user ID
                    "/queue/notifications",                 // destination
                    notification                             // payload
            );

            System.out.println("✅ Notification sent to " + event.getRecipientId());

        } catch (Exception e) {
            System.err.println("❌ Failed to process FollowEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

}