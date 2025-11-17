package com.fc.chatservice.service;

import com.fc.chatservice.dto.UserPresence;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public UserPresence getPresence(String email) {
        Boolean isOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, email);

        logger.debug("Checking presence for userId: [{}], isOnline: {}", email, isOnline);
        UserPresence result;
        if (Boolean.TRUE.equals(isOnline)) {
            logger.debug("presence true for {}", email);
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
        logger.info("User set as ONLINE in Redis: {}", email);
    }

    public void setUserOffline(String email) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, email);
        redisTemplate.opsForValue().set(
                LAST_SEEN_KEY_PREFIX + email,
                LocalDateTime.now().toString()
        );
        logger.info("User set as OFFLINE in Redis: {}", email);
    }
}
