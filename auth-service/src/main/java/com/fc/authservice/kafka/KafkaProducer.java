package com.fc.authservice.kafka;

import com.fc.notification.FollowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * KafkaProducer is responsible for publishing FollowEvent messages
 * to the Kafka topic "follow-events".
 * Each event is serialized into bytes and sent using a KafkaTemplate.
 * The recipientId is used as the Kafka partition key to ensure
 * proper ordering and distribution.
 */
@Slf4j
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a FollowEvent message to the "follow-events" Kafka topic.
     *
     * @param event the FollowEvent protobuf object containing sender and recipient IDs
     */
    public void sendFollowEvent(FollowEvent event) {
        String key = event.getRecipientId(); // Key used for partitioning the messages

        log.info("Preparing to send FollowEvent to Kafka. recipientId={}", key);

        try {
            // Convert FollowEvent protobuf to bytes and send to Kafka
            kafkaTemplate.send("follow-events", key, event.toByteArray());

            log.info("Successfully sent FollowEvent to Kafka. recipientId={}", key);

        } catch (Exception e) {

            log.error("Failed to send FollowEvent to Kafka for recipientId={}", key, e);
        }
    }
}
