package com.fc.postservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, json);
        } catch (Exception e) {
            // log error - do NOT let Kafka errors break moderation action
            // use monitoring/alerting for repeated failures
            throw new RuntimeException("Failed to publish to Kafka", e);
        }
    }
}
