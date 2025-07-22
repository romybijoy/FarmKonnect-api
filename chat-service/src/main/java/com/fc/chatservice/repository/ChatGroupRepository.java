package com.fc.chatservice.repository;

import com.fc.chatservice.model.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {
}
