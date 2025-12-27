package com.fc.postservice.service;

import com.fc.postservice.model.Like;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostLikeRepository;
import com.fc.postservice.repository.PostRepository;
import com.postservice.LikePostResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service responsible for handling post-like actions such as:
 * - toggle like/unlike
 * - check like state
 * - get like count
 * - manually like/unlike
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;

    /**
     * Toggles like status for a post. If user has already liked the post,
     * their like is removed; otherwise, a new like is added.
     *
     * @return LikePostResponse containing the new like state and updated count.
     */
    @Transactional
    public LikePostResponse handleLike(UUID postId, UUID userId) {

        log.info("Handling like toggle for postId={} by userId={}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found: {}", postId);
                    return new NoSuchElementException("Post not found");
                });

        boolean liked;

        // Toggle like state
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {

            log.debug("User already liked this post; removing like.");

            likeRepository.deleteByPostIdAndUserId(postId, userId);
            liked = false;
        } else {

            log.debug("User already liked this post; adding like.");

            Like like = new Like();
            like.setPost(post);
            like.setUserId(userId);
            likeRepository.save(like); // needs transaction
            liked = true;
        }

        long likeCount = likeRepository.countByPostId(postId);

        log.info("Like toggle complete | postId={}, liked={}, likeCount={}",
                postId, liked, likeCount);

        return LikePostResponse.newBuilder()
                .setLiked(liked)
                .setLikeCount((int) likeCount)
                .build();
    }

    // ---------- LIKE ----------

    /**
     * Adds a like only if the user has not liked the post already.
     */
    public void likePost(UUID postId, UUID userId) {
        log.info("Adding like for postId={} by userId={}", postId, userId);

        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> {
                        log.error("Post not found : {}", postId);
                        return new NoSuchElementException("Post not found");
                    });

            Like like = new Like();
            like.setUserId(userId);
            like.setPost(post);
            likeRepository.save(like);

            log.debug("Like saved successfully | postId={}, userId={}", postId, userId);
        }
    }

    /**
     * Removes a like for a given post and user.
     */
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        log.info("Removing like for postId={} by userId={}", postId, userId);
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /**
     * Checks whether a specific user has liked a specific post.
     */
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        boolean exists = likeRepository.existsByUserIdAndPostId(userId, postId);
        log.debug("Checking like status | postId={}, userId={}, liked={}", postId, userId, exists);
        return exists;
    }

    /**
     * Get the total number of likes on a post.
     */
    public long getLikeCount(UUID postId) {
        long count = likeRepository.countByPostId(postId);

        log.debug("Like count fetched | postId={}, count={}", postId, count);
        return count;
    }


}

