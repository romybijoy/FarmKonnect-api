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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<AdminPostDto> pagePosts = listPosts(pageable);

        List<AdminPostDto> posts = pagePosts.getContent();

        if (posts.isEmpty()) {
            throw new APIException("No posts found", 404);
        }

        PostResponse postsResponse = new PostResponse();
        postsResponse.setMessage("Posts fetched successfully");
        postsResponse.setStatusCode(302);
        postsResponse.setContent(posts);
        postsResponse.setPageNumber(pagePosts.getNumber());
        postsResponse.setPageSize(pagePosts.getSize());
        postsResponse.setTotalElements(pagePosts.getTotalElements());
        postsResponse.setTotalPages(pagePosts.getTotalPages());
        postsResponse.setLastPage(pagePosts.isLast());

        return postsResponse;
    }

    public Page<AdminPostDto> listPosts(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);

        // collect post ids from the current page
        List<UUID> postIds = page.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // get counts as maps
        Map<UUID, Long> commentCounts = commentRepository.countMapByPostIds(postIds);
        Map<UUID, Long> likeCounts = likeRepository.countMapByPostIds(postIds);
        Map<UUID, Long> saveCounts = saveRepository.countMapByPostIds(postIds);

        // map Post -> AdminPostDto, using 0 when count missing
        Page<AdminPostDto> dtoPage = page.map(p -> {
            long comments = commentCounts.getOrDefault(p.getId(), 0L);
            long likes = likeCounts.getOrDefault(p.getId(), 0L);
            long saves = saveCounts.getOrDefault(p.getId(), 0L);

            String preview = p.getContent() == null ? "" :
                    (p.getContent().length() > 200 ? p.getContent().substring(0, 200) + "..." : p.getContent());

            return new AdminPostDto(
                    p.getId(),
                    p.getUserId(),
                    p.getUserName(),
                    preview,
                    p.getPostImage(),
                    p.getCreatedAt(),
                    p.isRepost(),
                    p.getOriginalPostId(),
                    (int) comments, // if you prefer long counts, adjust DTO
                    (int) saves,
                    (int) likes
            );
        });

        return dtoPage;
    }

    public PostDetailAdminDto getPostDetail(UUID postId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));

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
        dto.setPostImage(p.getPostImage());
        dto.setDistrict(p.getDistrict());
        dto.setDescription(p.getDescription());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setRepost(p.isRepost());
        dto.setOriginalPostId(p.getOriginalPostId());
        dto.setCommentCount((int) comments);
        dto.setSaveCount((int) saves);
        dto.setLikeCount((int) likes);

        return dto;
    }
}

