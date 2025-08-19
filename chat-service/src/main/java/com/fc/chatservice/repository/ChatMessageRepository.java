package com.fc.chatservice.repository;

import com.fc.chatservice.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySenderIdOrReceiverIdOrderByTimestampAsc(UUID fromUserId, UUID toUserId);

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :from AND m.receiverId = :to) OR (m.senderId = :to AND m.receiverId = :from) ORDER BY m.timestamp ASC")
    List<ChatMessage> findBySenderAndReceiver(@Param("from") UUID senderId, @Param("to") UUID receiverId);


    List<ChatMessage> findByGroupIdOrderByTimestampAsc(UUID groupId);
}
