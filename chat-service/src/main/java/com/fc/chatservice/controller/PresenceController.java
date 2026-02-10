package com.fc.chatservice.controller;

import com.fc.chatservice.dto.UserPresence;
import com.fc.chatservice.service.UserPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final UserPresenceService presenceService;

    public PresenceController(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserPresence> getUserPresence(@PathVariable UUID userId) {
        UserPresence presence = presenceService.getPresence(userId);
        return ResponseEntity.ok(presence);
    }
}