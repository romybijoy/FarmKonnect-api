package com.fc.chatservice.config.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Presence Cleanup Job
 *
 * @author Romyb
 * @since 04/01/2026
 */
@Service
@EnableScheduling
public class PresenceCleanupJob {

    private final RedisTemplate<String, String> redisTemplate;

    public PresenceCleanupJob(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void cleanupExpiredUsers() {

        Set<String> onlineUsers =
                redisTemplate.opsForSet().members("online_users");

        if (onlineUsers == null) return;

        for (String userId : onlineUsers) {

            Boolean alive =
                    redisTemplate.hasKey("presence:" + userId);

            if (!alive) {

                // USER IS ACTUALLY OFFLINE
                redisTemplate.opsForSet().remove("online_users", userId);

                redisTemplate.opsForValue().set(
                        "last_seen:" + userId,
                        LocalDateTime.now().toString()
                );
            }
        }
    }
}
