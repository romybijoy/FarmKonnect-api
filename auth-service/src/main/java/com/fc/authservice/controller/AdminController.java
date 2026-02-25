package com.fc.authservice.controller;

import com.fc.authservice.dto.RecentUserDto;
import com.fc.authservice.dto.UserStatsDto;
import com.fc.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin controller
 *
 * @author Romyb
 * @since 25/02/2026
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/recent-users")
    public ResponseEntity<List<RecentUserDto>> getRecentUsers(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(userService.getRecentUsers(limit));
    }

    @GetMapping("/user-stats")
    public ResponseEntity<UserStatsDto> getUserStats() {
        return ResponseEntity.ok(userService.getUserStats());
    }
}
