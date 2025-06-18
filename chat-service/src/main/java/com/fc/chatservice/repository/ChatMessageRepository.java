package com.fc.chatservice.repository;

import com.fc.chatservice.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySenderIdOrReceiverIdOrderByTimestampAsc(UUID fromUserId, UUID toUserId);

    List<ChatMessage> findByGroupIdOrderByTimestampAsc(UUID groupId);
}
