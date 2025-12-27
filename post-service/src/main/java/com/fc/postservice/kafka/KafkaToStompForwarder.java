package com.fc.postservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens to Kafka moderation/report topics and forwards messages
 * to WebSocket clients via STOMP destinations.

 * This enables real-time updates in the Admin Dashboard such as:
 *  - New reports created
 *  - Reports reviewed by admins
 *  - Posts moderated/removed/restored
 */
@Slf4j
@Component
public class KafkaToStompForwarder {

    private final SimpMessagingTemplate simp;

    public KafkaToStompForwarder(SimpMessagingTemplate simp) {
        this.simp = simp;
    }

    /**
     * When a new report is created, notify all subscribed admin clients.
     */
    @KafkaListener(topics = "reports.created")
    public void onReportCreated(String payload) {
        log.info("Received Kafka message on 'reports.created': {}", payload);
        simp.convertAndSend("/topic/reports.created", payload);
    }

    /**
     * When a report is reviewed (approved/dismissed/actioned), broadcast update.
     */
    @KafkaListener(topics = "reports.reviewed")
    public void onReportReviewed(String payload) {
        log.info("Received Kafka message on 'reports.reviewed': {}", payload);
        simp.convertAndSend("/topic/reports.reviewed", payload);
    }

    /**
     * When a post is moderated (removed, restored), notify admin clients.
     */
    @KafkaListener(topics = "posts.moderated")
    public void onPostModerated(String payload) {
        log.info("Received Kafka message on 'posts.moderated': {}", payload);
        simp.convertAndSend("/topic/posts.moderated", payload);
    }
}
