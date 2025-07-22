package com.fc.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UserPresence {
    private boolean online;
    private LocalDateTime lastSeen;

    public UserPresence(boolean online, LocalDateTime lastSeen) {
        this.online = online;
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "UserPresence{" +
                "online=" + online +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
