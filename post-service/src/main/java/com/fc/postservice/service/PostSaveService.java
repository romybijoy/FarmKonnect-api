package com.fc.postservice.service;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.model.Post;
import com.fc.postservice.model.Save;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.SavedPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service responsible for managing saved posts:
 * - Save a post
 * - Unsave post
 * - Check saved status
 * - Get save counts
 * - Fetch all saved posts for a user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostSaveService {

    private final PostRepository postRepository;
    private final SavedPostRepository saveRepository;

    @Autowired
    public ModelMapper modelMapper;

    /**
     * Saves a post for a user if not already saved.
     */
    @Transactional
    public void savePost(UUID postId, UUID userId) {
        log.info("Saving postId={} for userId={}", postId, userId);

        if (!saveRepository.existsByUserIdAndPostId(userId, postId)) {

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> {
                        log.error("Post not found: {}", postId);
                        return new NoSuchElementException("Post not found");
                    });

            Save save = new Save();
            save.setUserId(userId);
            save.setPost(post);

            saveRepository.save(save);

            log.debug("Post saved successfully | postId={}, userId={}", postId, userId);
        } else {
            log.debug("Post already saved | postId={}, userId={}", postId, userId);
        }
    }

    /**
     * Removes a saved post-entry for the given user.
     */
    @Transactional
    public void unsavePost(UUID postId, UUID userId) {
        log.info("Unsaving postId={} for userId={}", postId, userId);
        saveRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /**
     * Checks whether the user has saved a specific post.
     */
    public boolean isPostSavedByUser(UUID postId, UUID userId) {
        boolean saved = saveRepository.existsByUserIdAndPostId(userId, postId);
        log.debug("Checking save status | postId={}, userId={}, saved={}", postId, userId, saved);
        return saved;
    }

    /**
     * Returns how many users have saved the post.
     */
    public long getSaveCount(UUID postId) {

        long count = saveRepository.countByPostId(postId);
        log.debug("Save count fetched | postId={}, count={}", postId, count);
        return count;
    }

    /**
     * Fetches all posts saved by a user.
     */
    public List<PostDTO> getSavedPosts(UUID userId) {
        log.info("Fetching saved posts for userId={}", userId);

        List<Save> saves = saveRepository.findByUserId(userId);
        return saves.stream()
                .map(save -> modelMapper.map(save.getPost(), PostDTO.class)) // map the actual Post
                .toList();
    }


}

