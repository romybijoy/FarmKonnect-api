package com.fc.chatservice.dto;

import lombok.Data;

@Data
public class TypingStatusDTO {

    private String email;       // who is typing
    private String receiverId;  // to whom
    private String groupId;
    private boolean isTyping;
}
