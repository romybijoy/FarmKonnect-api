package com.fc.chatservice.controller;

import com.fc.chatservice.dto.ReactionDTO;
import com.fc.chatservice.dto.ReactionResponse;
import com.fc.chatservice.model.MessageReaction;
import com.fc.chatservice.service.MessageReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class MessageReactionController {

    private final MessageReactionService reactionService;

    @PostMapping("/{messageId}")
    public MessageReaction addOrUpdateReaction(
            @PathVariable UUID messageId,
            @RequestBody ReactionDTO reactionDTO // contains userId and emoji
    ) {
        return reactionService.reactToMessage(messageId, reactionDTO.getUserId(), reactionDTO.getEmoji());
    }

    @GetMapping("/{messageId}")
    public List<ReactionResponse> getReactions(@PathVariable UUID messageId) {
        return reactionService.getReactionsWithUser(messageId);
    }

    @DeleteMapping("/{messageId}")
    public void removeReaction(
            @PathVariable UUID messageId,
            @RequestParam UUID userId
    ) {
        reactionService.removeReaction(messageId, userId);
    }
}
