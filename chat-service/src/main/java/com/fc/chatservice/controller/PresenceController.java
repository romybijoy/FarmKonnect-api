package com.fc.chatservice.controller;

import com.fc.chatservice.dto.UserPresence;
import com.fc.chatservice.service.UserPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final UserPresenceService presenceService;

    public PresenceController(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserPresence> getUserPresence(@PathVariable String email) {
        UserPresence presence = presenceService.getPresence(email);
        return ResponseEntity.ok(presence);
    }
}