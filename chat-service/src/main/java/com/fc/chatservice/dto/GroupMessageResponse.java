package com.fc.chatservice.dto;

import com.fc.chatservice.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
/**
 * Group Message Response
 *
 * @author Romyb
 * @since 16/02/2026
 */

@Data
public class GroupMessageResponse {

    public UUID id;
    public UUID senderId;
    public UUID receiverId;
    public UUID groupId;
    public String content;
    public String type;
    public String fileUrl;
    public String fileName;
    public LocalDateTime timestamp;
    public MessageStatus status;

    public Set<UUID> deliveredTo;
    public Set<UUID> readBy;

    public int deliveredCount;
    public int readCount;
    public int totalMembers;
}
