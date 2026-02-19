package com.fc.postservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fc.postservice.model.Post;
import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.service.ModerationService;

import java.util.List;

/**
 * Moderation Retry Scheduler for update post status to active
 *
 * @author Romyb
 * @since 19/02/2026
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationRetryScheduler {

    private final PostRepository postRepository;
    private final ModerationService moderationService;

    // Runs every 1 minute
    @Scheduled(fixedDelay = 60000)
    public void retryPendingModeration() {

        List<Post> pendingPosts =
                postRepository.findByStatus(PostStatus.PENDING);

        if (pendingPosts.isEmpty()) {
            return;
        }

        log.info("Retrying AI moderation for {} pending posts",
                pendingPosts.size());

        for (Post post : pendingPosts) {
            try {
                moderationService.validatePostAsync(post.getId());
            } catch (Exception e) {
                log.error("Retry failed for postId={}", post.getId());
            }
        }
    }
}

