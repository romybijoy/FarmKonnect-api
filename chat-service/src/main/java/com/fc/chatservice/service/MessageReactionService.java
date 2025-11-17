package com.fc.chatservice.service;

import com.fc.chatservice.dto.ReactionEvent;
import com.fc.chatservice.dto.ReactionResponse;
import com.fc.chatservice.model.MessageReaction;
import com.fc.chatservice.repository.MessageReactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageReactionService {

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    private final MessageReactionRepository reactionRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ReactionResponse> getReactionsWithUser(UUID messageId) {
        List<MessageReaction> reactions = reactionRepo.findByMessageId(messageId);

        return reactions.stream().map(reaction -> {
            UserRequest request = UserRequest.newBuilder()
                    .setUserId(reaction.getUserId().toString())
                    .build();

            UserResponse user = userStub.getUserById(request);

            return new ReactionResponse(
                    reaction.getEmoji(),
                    reaction.getUserId(),
                    user.getUserName(),
                    user.getImage()
            );
        }).toList();
    }

    public MessageReaction reactToMessage(UUID messageId, UUID userId, String emoji) {
        MessageReaction reaction = reactionRepo.findByMessageIdAndUserId(messageId, userId)
                .map(existing -> {
                    existing.setEmoji(emoji);
                    existing.setReactedAt(LocalDateTime.now());
                    return reactionRepo.save(existing);
                })
                .orElseGet(() -> reactionRepo.save(
                        MessageReaction.builder()
                                .messageId(messageId)
                                .userId(userId)
                                .emoji(emoji)
                                .reactedAt(LocalDateTime.now())
                                .build()));

        messagingTemplate.convertAndSend(
                "/topic/reactions/" + messageId,
                ReactionEvent.builder()
                        .messageId(messageId)
                        .userId(userId)
                        .emoji(emoji)
                        .type("ADD")
                        .build()
        );

        return reaction;
    }

    @Transactional
    public void removeReaction(UUID messageId, UUID userId) {
        reactionRepo.deleteByMessageIdAndUserId(messageId, userId);

        messagingTemplate.convertAndSend(
                "/topic/reactions/" + messageId,
                ReactionEvent.builder()
                        .messageId(messageId)
                        .userId(userId)
                        .emoji(null)
                        .type("REMOVE")
                        .build()
        );
    }
}
