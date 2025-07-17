package com.fc.authservice.kafka;

import com.fc.notification.FollowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendFollowEvent(FollowEvent event) {
        try {
            String key = event.getRecipientId(); // recipientId helps in partitioning
            kafkaTemplate.send("follow-events", key, event.toByteArray());
            log.info("✅ Sent FollowEvent to Kafka for recipient ID: {}", key);
        } catch (Exception e) {
            log.error("❌ Failed to send FollowEvent to Kafka", e);
        }
    }
}
