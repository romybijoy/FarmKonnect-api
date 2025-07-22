package com.fc.chatservice.service;

import com.fc.chatservice.dto.UserPresence;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserPresenceService {

    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String LAST_SEEN_KEY_PREFIX = "last_seen:";

    private final RedisTemplate<String, String> redisTemplate;

    public UserPresenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public UserPresence getPresence(String email) {
        Boolean isOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, email);

        System.out.println("Checking presence for userId: [" + email + "], isOnline: " + isOnline+"]");
        UserPresence result;
        if (Boolean.TRUE.equals(isOnline)) {
            System.out.println("presence true");
            result = new UserPresence(true, null);
            return result;
        } else {
            String lastSeenStr = redisTemplate.opsForValue().get(LAST_SEEN_KEY_PREFIX + email);
            LocalDateTime lastSeen = null;

            if (lastSeenStr != null) {
                lastSeen = LocalDateTime.parse(lastSeenStr);
            }
            result = new UserPresence(false, lastSeen);
            return result;
        }
    }

    // Optionally: Call when user connects/disconnects
    public void setUserOnline(String email) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, email);
        System.out.println("User set as ONLINE in Redis: " + email);
    }

    public void setUserOffline(String email) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, email);
        redisTemplate.opsForValue().set(
                LAST_SEEN_KEY_PREFIX + email,
                LocalDateTime.now().toString()
        );
        System.out.println("User set as OFFLINE in Redis: " + email);
    }
}

