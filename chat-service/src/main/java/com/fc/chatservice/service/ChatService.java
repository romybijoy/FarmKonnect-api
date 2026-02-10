package com.fc.chatservice.service;

import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatMessageRepository repository;

    public ChatService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return repository.save(message);
    }

    public List<ChatMessage> getPrivateChat(UUID senderId, UUID receiverId) {
        return repository.findBySenderAndReceiver(senderId, receiverId);
    }

    public List<ChatMessage> getMessagesByGroup(UUID groupId) {
        return repository.findByGroupIdOrderByTimestampAsc(groupId);
    }

        // ---------------- Delete for Me ----------------
        public ChatMessage deleteForMe(UUID messageId, UUID userId) {
            ChatMessage msg = repository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            Set<String> deletedUsers = new HashSet<>();

            if (msg.getDeletedBy() != null) {
                deletedUsers.addAll(Arrays.asList(msg.getDeletedBy().split(",")));
            }

            deletedUsers.add(String.valueOf(userId));
            msg.setDeletedBy(String.join(",", deletedUsers));

            return repository.save(msg);
        }

        // ---------------- Delete for Everyone ----------------
        public ChatMessage deleteForEveryone(UUID messageId, UUID userId) {
            ChatMessage msg = repository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            // Only sender can delete for everyone
            if (!msg.getSenderId().equals(userId)) {
                throw new RuntimeException("Not authorized");
            }

            msg.setDeletedForAll(true);
            msg.setContent(null);
            msg.setFileUrl(null);
            msg.setFileName(null);
            msg.setType("deleted");

            return repository.save(msg);
        }


}
