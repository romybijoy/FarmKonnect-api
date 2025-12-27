package com.fc.postservice.service;

import com.fc.postservice.dto.admin.AdminPostDto;
import com.fc.postservice.dto.admin.PostDetailAdminDto;
import com.fc.postservice.dto.admin.PostResponse;
import com.fc.postservice.exception.APIException;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.CommentRepository;
import com.fc.postservice.repository.PostLikeRepository;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.SavedPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin service for managing, listing, and inspecting posts.
 * Includes post counts for comments, likes, and saves, along with pagination.
 */
@Slf4j
@Service
public class AdminPostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final SavedPostRepository saveRepository;

    public AdminPostService(PostRepository postRepository,
                            CommentRepository commentRepository,
                            PostLikeRepository likeRepository,
                            SavedPostRepository saveRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.saveRepository = saveRepository;
    }

    /**
     * Fetch paginated list of posts with aggregated statistics.
     */
    public PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        log.info("Admin fetching all posts | page={}, size={}, sortBy={}, sortOrder={}",
                pageNumber, pageSize, sortBy, sortOrder);

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<AdminPostDto> pagePosts = listPosts(pageable);

        if (pagePosts.isEmpty()) {
            log.warn("Admin post fetch returned no posts");
            throw new APIException("No posts found", 404);
        }

        PostResponse response = new PostResponse();
        response.setMessage("Posts fetched successfully");
        response.setStatusCode(302);
        response.setContent(pagePosts.getContent());
        response.setPageNumber(pagePosts.getNumber());
        response.setPageSize(pagePosts.getSize());
        response.setTotalElements(pagePosts.getTotalElements());
        response.setTotalPages(pagePosts.getTotalPages());
        response.setLastPage(pagePosts.isLast());

        log.info("Admin post fetch successful | totalElements={}", pagePosts.getTotalElements());

        return response;
    }

    /**
     * Prevent overflow when converting long → int.
     */
    private static int safeLongToInt(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    /**
     * Internal method for mapping posts to AdminPostDto with metrics.
     */
    public Page<AdminPostDto> listPosts(Pageable pageable) {

        log.debug("Fetching posts with pageable: {}", pageable);

        Page<Post> page = postRepository.findAll(pageable);

        // collect post ids from the current page
        List<UUID> postIds = page.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.debug("Collected {} postIds for aggregation", postIds.size());

        // aggregated counts
        Map<UUID, Long> commentCounts = commentRepository.countMapByPostIds(postIds);
        Map<UUID, Long> likeCounts = likeRepository.countMapByPostIds(postIds);
        Map<UUID, Long> saveCounts = saveRepository.countMapByPostIds(postIds);

        return page.map(p -> {
            long comments = commentCounts.getOrDefault(p.getId(), 0L);
            long likes = likeCounts.getOrDefault(p.getId(), 0L);
            long saves = saveCounts.getOrDefault(p.getId(), 0L);

            int commentCount = safeLongToInt(comments);
            int likeCount = safeLongToInt(likes);
            int saveCount = safeLongToInt(saves);

            String preview = p.getContent() == null ? "" : p.getContent();

            return AdminPostDto.builder()
                    .postId(p.getId())
                    .userId(p.getUserId())
                    .userName(p.getUserName())
                    .contentPreview(preview)
                    .postImages(p.getPostImages())
                    .createdAt(p.getCreatedAt())
                    .isRepost(p.isRepost())
                    .originalPostId(p.getOriginalPostId())
                    .commentCount(commentCount)
                    .saveCount(saveCount)
                    .likeCount(likeCount)
                    .build();
        });
    }

    /**
     * Detailed admin view of a single post with all metrics.
     */
    public PostDetailAdminDto getPostDetail(UUID postId) {
        log.info("Fetching post detail for postId={}", postId);

        Post p = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found: {}", postId);
                    return new NoSuchElementException("Post not found: " + postId);
                });

        long comments = commentRepository.countMapByPostIds(Collections.singletonList(postId))
                .getOrDefault(postId, 0L);
        long likes = likeRepository.countMapByPostIds(Collections.singletonList(postId))
                .getOrDefault(postId, 0L);
        long saves = saveRepository.countMapByPostIds(Collections.singletonList(postId))
                .getOrDefault(postId, 0L);

        PostDetailAdminDto dto = new PostDetailAdminDto();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        dto.setUserName(p.getUserName());
        dto.setContent(p.getContent());
        dto.setPostImages(p.getPostImages());
        dto.setDistrict(p.getDistrict());
        dto.setDescription(p.getDescription());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setRepost(p.isRepost());
        dto.setOriginalPostId(p.getOriginalPostId());
        dto.setCommentCount((int) comments);
        dto.setSaveCount((int) saves);
        dto.setLikeCount((int) likes);

        log.info("Post detail fetched successfully for postId={}", postId);

        return dto;
    }
}

