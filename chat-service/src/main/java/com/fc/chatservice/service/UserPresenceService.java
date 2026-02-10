package com.fc.chatservice.service;

import com.fc.chatservice.dto.UserPresence;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserPresenceService {

    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String LAST_SEEN_KEY_PREFIX = "last_seen:";

    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UserPresenceService.class);

    public UserPresenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public UserPresence getPresence(UUID userId) {
        String key = userId.toString();

        // 1️⃣ ONLINE check
        Boolean isOnline =
                redisTemplate.opsForSet().isMember("online_users", key);

        if (Boolean.TRUE.equals(isOnline)) {
            return new UserPresence(true, null);
        }

        // 2️⃣ FALLBACK: last seen
        String lastSeenStr =
                redisTemplate.opsForValue().get("last_seen:" + key);

        if (lastSeenStr != null) {
            return new UserPresence(false, LocalDateTime.parse(lastSeenStr));
        }

        // 3️⃣ FINAL fallback (never logged in / unknown)
        return new UserPresence(false, null);
    }


    public void setUserOnline(UUID userId) {
        String key = userId.toString();

        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, key);

        // 🔥 add heartbeat TTL (2 minutes)
        redisTemplate.opsForValue().set(
                "presence:" + key,
                "online",
                Duration.ofMinutes(2)
        );
    }


    public void setUserOffline(UUID userId) {
        String key = userId.toString();

        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, key);
        redisTemplate.opsForValue().set(
                LAST_SEEN_KEY_PREFIX + key,
                LocalDateTime.now().toString()
        );

        logger.info("User set as OFFLINE in Redis: {}", key);
    }
}

