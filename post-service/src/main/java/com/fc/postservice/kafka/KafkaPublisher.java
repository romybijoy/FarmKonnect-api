package com.fc.postservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void publish(String topic, byte[] payload) {
        kafkaTemplate.send(topic, payload);
        log.info("Published Protobuf event to topic={}", topic);
    }
}