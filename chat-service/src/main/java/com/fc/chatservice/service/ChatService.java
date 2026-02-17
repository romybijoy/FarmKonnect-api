package com.fc.chatservice.service;

import com.fc.chatservice.dto.GroupMessageResponse;
import com.fc.chatservice.enums.MessageStatus;
import com.fc.chatservice.model.ChatMessage;
import com.fc.chatservice.repository.ChatMessageRepository;
import com.fc.chatservice.repository.GroupMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatMessageRepository repository;

    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository repository, GroupMemberRepository groupMemberRepository) {
        this.repository = repository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return repository.save(message);
    }

    public List<ChatMessage> getPrivateChat(UUID senderId, UUID receiverId) {
        return repository.findBySenderAndReceiver(senderId, receiverId);
    }

    public List<GroupMessageResponse> getMessagesByGroup(UUID groupId) {

        int totalMembers = groupMemberRepository.countByGroupId(groupId);

        return repository.findByGroupIdOrderByTimestampAsc(groupId)
                .stream()
                .map(msg -> {

                    GroupMessageResponse dto = new GroupMessageResponse();

                    dto.id = msg.getId();
                    dto.senderId = msg.getSenderId();
                    dto.receiverId = msg.getReceiverId();
                    dto.groupId = msg.getGroupId();
                    dto.content = msg.getContent();
                    dto.type = msg.getType();
                    dto.fileUrl = msg.getFileUrl();
                    dto.fileName = msg.getFileName();
                    dto.timestamp = msg.getTimestamp();
                    dto.status = msg.getStatus();

                    dto.deliveredTo = msg.getDeliveredTo();
                    dto.readBy = msg.getReadBy();

                    dto.deliveredCount = msg.getDeliveredTo().size();
                    dto.readCount = msg.getReadBy().size();
                    dto.totalMembers = totalMembers;

                    return dto;
                })
                .toList();
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


    // -------------------------------
    // MARK AS DELIVERED
    // -------------------------------
    @Transactional
    public void markPrivateMessagesAsDelivered(UUID senderId, UUID receiverId) {

        List<ChatMessage> messages =
                repository.findBySenderIdAndReceiverIdAndStatus(
                        senderId,
                        receiverId,
                        MessageStatus.SENT
                );

        messages.forEach(m -> m.setStatus(MessageStatus.DELIVERED));
        repository.saveAll(messages);

        notifySender(messages, senderId);
    }


    // -------------------------------
    // MARK AS READ
    // -------------------------------
    @Transactional
    public void markPrivateMessagesAsRead(UUID senderId, UUID receiverId) {

        List<ChatMessage> messages =
               repository.findBySenderIdAndReceiverIdAndStatusIn(
                        senderId,
                        receiverId,
                        List.of(MessageStatus.SENT, MessageStatus.DELIVERED)
                );

        messages.forEach(m -> m.setStatus(MessageStatus.READ));
        repository.saveAll(messages);

        notifySender(messages, senderId);
    }

    @Transactional
    public void markGroupMessageDelivered(UUID messageId, UUID userId) {

        ChatMessage message = repository.findById(messageId)
                .orElseThrow();

        // Skip if sender
        if (message.getSenderId().equals(userId)) return;

        boolean changed = false;

        // Add to delivered list
        if (!message.getDeliveredTo().contains(userId)) {
            message.getDeliveredTo().add(userId);
            changed = true;
        }

        if (!changed) return;

        updateGroupStatus(message);

        repository.save(message);

        notifyStatusUpdate(message);
    }



    @Transactional
    public void markGroupMessageRead(UUID messageId, UUID userId) {


        System.out.println("Marking message " + messageId + " as read by user " + userId);
        ChatMessage message = repository.findById(messageId)
                .orElseThrow();

        if (message.getSenderId().equals(userId)) return;

        boolean changed = false;

        if (!message.getDeliveredTo().contains(userId)) {
            message.getDeliveredTo().add(userId);
            changed = true;
        }

        if (!message.getReadBy().contains(userId)) {
            message.getReadBy().add(userId);
            changed = true;
        }

        if (!changed) return;

        updateGroupStatus(message);

        repository.save(message);

        notifyStatusUpdate(message);
    }


    private void updateGroupStatus(ChatMessage message) {

        int totalMembers = groupMemberRepository
                .countByGroupId(message.getGroupId());

        int totalRecipients = totalMembers - 1;

        System.out.println("Group Members: " + totalMembers);
        System.out.println("Delivered count: " + message.getDeliveredTo().size());
        System.out.println("Read count: " + message.getReadBy().size());

        if (message.getReadBy().size() == totalRecipients) {
            message.setStatus(MessageStatus.READ);
        }
        else if (message.getDeliveredTo().size() == totalRecipients) {
            message.setStatus(MessageStatus.DELIVERED);
        }
        else if (!message.getDeliveredTo().isEmpty()) {
            message.setStatus(MessageStatus.DELIVERED);
        }
        else {
            message.setStatus(MessageStatus.SENT);
        }
    }



    private void notifySender(List<ChatMessage> messages, UUID senderId) {

        for (ChatMessage message : messages) {

            messagingTemplate.convertAndSend(
                    "/topic/private/" + senderId,
                    message
            );
        }
    }

    private void notifyStatusUpdate(ChatMessage message) {

        int totalMembers = groupMemberRepository
                .countByGroupId(message.getGroupId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "STATUS_UPDATE");
        payload.put("messageId", message.getId());
        payload.put("status", message.getStatus());
        payload.put("groupId", message.getGroupId());
        payload.put("deliveredCount", message.getDeliveredTo().size());
        payload.put("readCount", message.getReadBy().size());
        payload.put("totalMembers", totalMembers);

        System.out.println(payload);
        // Notify sender
        messagingTemplate.convertAndSend(
                "/topic/private/" + message.getSenderId(),
                payload
        );

        messagingTemplate.convertAndSend(
                "/topic/group/" + message.getGroupId(),
                payload
        );


    }


}
