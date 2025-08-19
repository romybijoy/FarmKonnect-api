package com.fc.chatservice.service;

import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
}
