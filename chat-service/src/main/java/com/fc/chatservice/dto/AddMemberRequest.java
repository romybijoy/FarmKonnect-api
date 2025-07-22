package com.fc.chatservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequest {
    private UUID userId;
    private boolean isAdmin;
}