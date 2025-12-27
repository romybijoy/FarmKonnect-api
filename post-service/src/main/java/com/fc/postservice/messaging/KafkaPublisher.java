package com.fc.postservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Utility component responsible for publishing JSON messages to Kafka topics.
 * Responsibilities:
 *  - Serialize objects to JSON using Jackson
 *  - Publish messages to Kafka using KafkaTemplate
 *  - Ensure failures do not break moderation or other critical workflows
 * Notes:
 *  - Exceptions are wrapped in RuntimeException so the caller may decide
 *    to log, fallback, or trigger monitoring.
 *  - Logging is included to aid debugging and operational visibility.
 */
@Slf4j
@Component
public class KafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a JSON-serialized payload to the given Kafka topic.
     *
     * @param topic   the Kafka topic to publish to
     * @param payload the object to serialize as JSON
     */
    public void publish(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            log.debug("Publishing message to Kafka topic='{}' payload={}", topic, json);

            kafkaTemplate.send(topic, json);

            log.info("Successfully published message to topic='{}'", topic);
        } catch (Exception e) {
            // Log with full stack trace for diagnosis
            log.error("Failed to publish Kafka message to topic='{}': {}", topic, e.getMessage(), e);

            // Rethrow to allow upstream monitoring / fallback logic
            throw new RuntimeException("Failed to publish to Kafka", e);
        }
    }
}
