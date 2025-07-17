package com.fc.notificationservice.controller;


import com.fc.notificationservice.model.Notification;
import com.fc.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // 🔍 Get all notifications for a user
    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable String userId) {
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(userId);
    }

    // ✅ Mark a single notification as read
    @PatchMapping("/read/{id}")
    public Notification markAsRead(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // ✅ Mark all notifications for a user as read
    @PatchMapping("/read-all/{userId}")
    public List<Notification> markAllAsRead(@PathVariable String userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalse(userId);
        notifications.forEach(n -> n.setRead(true));
        return notificationRepository.saveAll(notifications);
    }
}
