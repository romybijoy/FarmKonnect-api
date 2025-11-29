package com.fc.postservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaToStompForwarder {

    private final SimpMessagingTemplate simp;

    public KafkaToStompForwarder(SimpMessagingTemplate simp) {
        this.simp = simp;
    }

    @KafkaListener(topics = "reports.created")
    public void onReportCreated(String payload) {
        // forward to connected admin clients
        simp.convertAndSend("/topic/reports.created", payload);
    }

    @KafkaListener(topics = "reports.reviewed")
    public void onReportReviewed(String payload) {
        simp.convertAndSend("/topic/reports.reviewed", payload);
    }

    @KafkaListener(topics = "posts.moderated")
    public void onPostModerated(String payload) {
        simp.convertAndSend("/topic/posts.moderated", payload);
    }
}
