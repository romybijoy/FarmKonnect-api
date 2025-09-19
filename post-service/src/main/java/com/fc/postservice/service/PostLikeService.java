package com.fc.postservice.service;

import com.fc.postservice.model.Like;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostLikeRepository;
import com.fc.postservice.repository.PostRepository;
import com.postservice.LikePostResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;

    @Transactional
    public LikePostResponse handleLike(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean liked;

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.deleteByPostIdAndUserId(postId, userId); // needs transaction
            liked = false;
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUserId(userId);
            likeRepository.save(like); // needs transaction
            liked = true;
        }

        long likeCount = likeRepository.countByPostId(postId);

        return LikePostResponse.newBuilder()
                .setLiked(liked)
                .setLikeCount((int) likeCount)
                .build();
    }

    // ---------- LIKE ----------
    public void likePost(UUID postId, UUID userId) {
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            Like like = new Like();
            like.setUserId(userId);
            like.setPost(postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found")));
            likeRepository.save(like);
        }
    }

    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }


    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    public long getLikeCount(UUID postId) {
        return likeRepository.countByPostId(postId);
    }


}

