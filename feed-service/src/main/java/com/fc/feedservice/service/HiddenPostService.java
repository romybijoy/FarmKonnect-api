package com.fc.feedservice.service;

import com.fc.feedservice.model.HiddenPost;
import com.fc.feedservice.repository.HiddenPostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HiddenPostService {
    private final HiddenPostRepository hiddenPostRepository;

    private static final Logger logger = LoggerFactory.getLogger(HiddenPostService.class);

    public HiddenPostService(HiddenPostRepository hiddenPostRepository) {
        this.hiddenPostRepository = hiddenPostRepository;
    }

    public void hidePost(UUID userId, UUID postId) {
        if (!hiddenPostRepository.existsByUserIdAndPostId(userId, postId)) {
            logger.debug("Hiding post: userId={}, postId={}", userId, postId);
            HiddenPost hiddenPost = new HiddenPost();
            hiddenPost.setUserId(userId);
            hiddenPost.setPostId(postId);
            hiddenPostRepository.save(hiddenPost);
        }
    }

    public List<UUID> getHiddenPostsForUser(UUID userId) {
        return hiddenPostRepository.findByUserId(userId)
                .stream()
                .map(HiddenPost::getPostId)
                .toList();
    }
}
