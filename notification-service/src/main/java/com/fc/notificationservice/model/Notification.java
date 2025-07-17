package com.fc.notificationservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    private String senderId;
    private String recipientId;
    private String senderName;
    private String senderProfilePic;
    private String message;
    private long timestamp;

    private boolean read = false;
}
