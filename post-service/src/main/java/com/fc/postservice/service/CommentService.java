package com.fc.postservice.service;

import com.fc.postservice.client.UserServiceClient;
import com.fc.postservice.dto.CommentDto;
import com.fc.postservice.dto.CommentRequest;
import com.fc.postservice.dto.ReactionDto;
import com.fc.postservice.dto.UserDto;
import com.fc.postservice.model.Comment;
import com.fc.postservice.model.CommentLike;
import com.fc.postservice.model.CommentReaction;
import com.fc.postservice.repository.CommentLikeRepository;
import com.fc.postservice.repository.CommentReactionRepository;
import com.fc.postservice.repository.CommentRepository;
import com.userproto.UserRequest;
import com.userproto.UserResponse;

import com.userproto.UserServiceGrpc;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that handles comment creation, replies, retrieval,
 * and recursive mapping of comment threads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReactionRepository commentReactionRepository;

    private final UserServiceClient userServiceClient;

    /**
     * Add a new comment to a post. Supports both root and nested replies.
     */
    @Transactional
    public CommentDto addComment(UUID postId,
                                 UUID userId,
                                 CommentRequest request) {

        log.info("User {} adding comment to post {}", userId, postId);

        String content = request.getContent() != null
                ? request.getContent().trim()
                : null;

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);

        if (request.getParentId() != null) {

            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> {
                        log.error("Parent comment not found: {}", request.getParentId());
                        return new NoSuchElementException("Parent comment not found");
                    });

            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }

            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);

        log.info("Comment saved successfully with id={}", saved.getId());

        return mapNewCommentToDto(saved, userId);
    }


    private CommentDto mapNewCommentToDto(Comment comment, UUID currentUserId) {

        UserDto user = userServiceClient.getUserById(comment.getUserId());

        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .parentId(
                comment.getParentComment() != null
                        ? comment.getParentComment().getId()
                        : null)
                .userName(user != null ? user.getName() : "Unknown")
                .profileImage(user != null ? user.getProfileImage(): null)
                .edited(false)
                .deleted(false)
                .likeCount(0)
                .replyCount(0L)
                .ownedByCurrentUser(true)
                .likedByCurrentUser(false)
                .reactions(List.of())
                .build();
    }

    /**
     * Fetch all top-level comments for a post along with their nested replies.
     */
    public List<CommentDto> getCommentsWithReplies(UUID postId) {

        log.info("Fetching comments with replies for postId={}", postId);

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        return comments.stream()
                .map(this::mapToDtoWithReplies)
                .toList();
    }

    /**
     * Recursive mapping from Comment → CommentDto including replies.
     */
    private CommentDto mapToDtoWithReplies(Comment comment) {
        UUID userId = comment.getUserId();
        if (userId == null) {
            log.error("UserId is null for commentId={}", comment.getId());
            throw new IllegalArgumentException("User ID must not be null");
        }

        UserDto user;
        try {
            user = userServiceClient.getUserById(userId);
        } catch (Exception e) {
            log.error("Failed to fetch user info for userId={}", userId, e);
            throw new RuntimeException("Failed to fetch user info", e);
        }

        // Build DTO with recursive replies
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.isDeleted()
                        ? "This comment was deleted"
                        : comment.getContent())
                .postId(comment.getPostId())
                .parentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .userId(user.getUserId())
                .userName(user.getName())
                .profileImage(user.getProfileImage())
                .replies(
                        comment.getReplies().stream()
                                .map(this::mapToDtoWithReplies)
                                .toList()
                )
                .build();
    }


    @Transactional(readOnly = true)
    public Page<CommentDto> getComments(UUID postId,
                                        UUID currentUserId,
                                        int page,
                                        int size) {

        log.info("Fetching comments. PostId={}, CurrentUserId={}, Page={}, Size={}",
                postId, currentUserId, page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Comment> commentPage =
                commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);

        List<Comment> comments = commentPage.getContent();

        log.debug("Comments fetched from DB. Count={}", comments.size());

        if (comments.isEmpty()) {
            log.info("No comments found for PostId={}", postId);
            return Page.empty(pageable);
        }

        // 1️⃣ Collect comment IDs
        List<UUID> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        log.debug("Collected commentIds. Count={}", commentIds.size());

        // 2️⃣ Collect userIds (parents + replies)
        Set<UUID> userIds = comments.stream()
                .flatMap(comment -> {
                    Stream<UUID> parentUser = Stream.of(comment.getUserId());
                    Stream<UUID> replyUsers = comment.getReplies()
                            .stream()
                            .map(Comment::getUserId);
                    return Stream.concat(parentUser, replyUsers);
                })
                .collect(Collectors.toSet());

        log.debug("Collected unique userIds. Count={}", userIds.size());

        // 3️⃣ Batch fetch users via gRPC
        Map<UUID, UserResponse> userMap =
                userServiceClient.getUsersByIds(userIds);

        log.debug("Users fetched via gRPC. Count={}", userMap.size());

        // 4️⃣ Batch load liked IDs
        Set<UUID> likedCommentIds =
                commentLikeRepository.findLikedCommentIdsByUserAndPost(currentUserId, postId);

        log.debug("Liked commentIds fetched. Count={}", likedCommentIds.size());

        // 5️⃣ Batch load reply counts
        Map<UUID, Long> replyCountMap =
                getReplyCountMap(postId);

        log.debug("ReplyCountMap loaded. Size={}", replyCountMap.size());

        // 6️⃣ Batch load reactions
        Map<UUID, List<ReactionDto>> reactionMap =
                getReactionMap(commentIds, currentUserId);

        log.debug("ReactionMap loaded. Size={}", reactionMap.size());

        // 7️⃣ Map DTO
        log.debug("Mapping comments to DTO...");

        Page<CommentDto> result = commentPage.map(comment ->
                mapToDto(comment,
                        currentUserId,
                        likedCommentIds,
                        replyCountMap,
                        reactionMap,
                        userMap)
        );

        log.info("Successfully returned paginated comments. TotalElements={}, TotalPages={}",
                result.getTotalElements(),
                result.getTotalPages());

        return result;
    }

    private Map<UUID, List<ReactionDto>> getReactionMap(
            List<UUID> commentIds,
            UUID currentUserId
    ) {

        List<Object[]> rows =
                commentReactionRepository
                        .aggregateReactionsForComments(commentIds, currentUserId);

        Map<UUID, List<ReactionDto>> reactionMap = new HashMap<>();

        for (Object[] row : rows) {

            UUID commentId = (UUID) row[0];
            String emoji = (String) row[1];
            Long count = (Long) row[2];
            Long userReactedCount = (Long) row[3];

            ReactionDto dto = ReactionDto.builder()
                    .emoji(emoji)
                    .count(count)
                    .reactedByCurrentUser(userReactedCount > 0)
                    .build();

            reactionMap
                    .computeIfAbsent(commentId, k -> new ArrayList<>())
                    .add(dto);
        }

        return reactionMap;
    }

    private Map<UUID, Long> getReplyCountMap(UUID postId) {

        List<Object[]> rows = commentRepository.fetchReplyCounts(postId);

        Map<UUID, Long> replyCountMap = new HashMap<>();

        for (Object[] row : rows) {
            UUID commentId = (UUID) row[0];
            Long count = (Long) row[1];
            replyCountMap.put(commentId, count);
        }

        return replyCountMap;
    }


    @Transactional(readOnly = true)
    public Page<CommentDto> getReplies(UUID parentId,
                                       UUID currentUserId,
                                       int page,
                                       int size) {

        log.info("Fetching replies. ParentId={}, CurrentUserId={}, Page={}, Size={}",
                parentId, currentUserId, page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").ascending()
        );

        Page<Comment> replyPage =
                commentRepository.findByParentComment_Id(parentId, pageable);

        log.debug("Replies fetched from DB. Count={}", replyPage.getContent().size());

        if (replyPage.isEmpty()) {
            log.info("No replies found for ParentId={}", parentId);
            return Page.empty(pageable);
        }

        Set<UUID> likedCommentIds =
                commentLikeRepository.findLikedCommentIdsByUser(currentUserId);

        log.debug("Liked reply IDs fetched. Count={}", likedCommentIds.size());

        log.debug("Mapping replies to DTO...");

        Page<CommentDto> result = replyPage.map(reply ->
                mapReplyToDto(reply, currentUserId, likedCommentIds)
        );

        log.info("Replies successfully returned. TotalElements={}, TotalPages={}",
                result.getTotalElements(),
                result.getTotalPages());

        return result;
    }

    private CommentDto mapReplyToDto(Comment reply,
                                     UUID currentUserId,
                                     Set<UUID> likedCommentIds) {

        boolean ownedByCurrentUser =
                reply.getUserId().equals(currentUserId);

        boolean likedByCurrentUser =
                likedCommentIds.contains(reply.getId());

        log.debug("Mapping reply. ReplyId={}, UserId={}, OwnedByCurrentUser={}, LikedByCurrentUser={}",
                reply.getId(),
                reply.getUserId(),
                ownedByCurrentUser,
                likedByCurrentUser);

        return CommentDto.builder()
                .id(reply.getId())
                .postId(reply.getPostId())
                .userId(reply.getUserId())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .edited(reply.isEdited())
                .deleted(reply.isDeleted())
                .likeCount(reply.getLikeCount())
                .replyCount(0L)
                .ownedByCurrentUser(ownedByCurrentUser)
                .likedByCurrentUser(likedByCurrentUser)
                .reactions(mapReactions(reply, currentUserId))
                .build();
    }


    private CommentDto mapToDto(Comment comment,
                                UUID currentUserId,
                                Set<UUID> likedCommentIds,
                                Map<UUID, Long> replyCountMap,
                                Map<UUID, List<ReactionDto>> reactionMap,
                                Map<UUID, UserResponse> userMap) {

        log.debug("Mapping Comment to DTO. CommentId={}, PostId={}, UserId={}",
                comment.getId(), comment.getPostId(), comment.getUserId());

        UserResponse user = userMap.get(comment.getUserId());

        if (user == null) {
            log.warn("User not found in userMap for UserId={}", comment.getUserId());
        } else {
            log.debug("User fetched successfully. Username={}, ProfileImagePresent={}",
                    user.getUserName(),
                    user.getImage() != null);
        }

        String userName = user != null ? user.getUserName() : "Unknown";
        String profileImage = user != null ? user.getImage() : null;

        Long replyCount = replyCountMap.getOrDefault(comment.getId(), 0L);
        boolean ownedByCurrentUser = comment.getUserId().equals(currentUserId);
        boolean likedByCurrentUser = likedCommentIds.contains(comment.getId());
        List<ReactionDto> reactions =
                reactionMap.getOrDefault(comment.getId(), List.of());

        log.debug("Comment Stats -> ReplyCount={}, LikeCount={}, OwnedByCurrentUser={}, LikedByCurrentUser={}, ReactionCount={}",
                replyCount,
                comment.getLikeCount(),
                ownedByCurrentUser,
                likedByCurrentUser,
                reactions.size());

        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.isDeleted()
                        ? "This comment was deleted"
                        : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userName(userName)
                .profileImage(profileImage)
                .edited(comment.isEdited())
                .deleted(comment.isDeleted())
                .likeCount(comment.getLikeCount())
                .replyCount(replyCount)
                .ownedByCurrentUser(ownedByCurrentUser)
                .likedByCurrentUser(likedByCurrentUser)
                .reactions(reactions)
                .build();
    }

    private List<ReactionDto> mapReactions(Comment comment,
                                           UUID currentUserId) {

        if (comment.getReactions() == null) return List.of();

        Map<String, List<CommentReaction>> grouped =
                comment.getReactions()
                        .stream()
                        .collect(Collectors.groupingBy(CommentReaction::getEmoji));

        return grouped.entrySet()
                .stream()
                .map(entry -> ReactionDto.builder()
                        .emoji(entry.getKey())
                        .count(entry.getValue().size())
                        .reactedByCurrentUser(
                                entry.getValue()
                                        .stream()
                                        .anyMatch(r ->
                                                r.getUserId().equals(currentUserId))
                        )
                        .build()
                )
                .toList();
    }
    /**
     * Get the number of top-level comments for a post.
     */
    public long getCommentCount(UUID postId) {
        log.debug("Counting top-level comments for postId={}", postId);
        return commentRepository.countTopLevelCommentsByPostId(postId);
    }

    @Transactional
    public void toggleLike(UUID commentId, UUID userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<CommentLike> existing =
                commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUserId(userId);
            commentLikeRepository.save(like);

            comment.setLikeCount(comment.getLikeCount() + 1);
        }
    }

    @Transactional
    public void toggleReaction(UUID commentId, UUID userId, String emoji) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Comment not found"
                        )
                );

        if (comment.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot react to deleted comment"
            );
        }

        Optional<CommentReaction> existing =
                commentReactionRepository.findByCommentIdAndUserId(commentId, userId);

        if (existing.isPresent()) {

            CommentReaction reaction = existing.get();

            if (reaction.getEmoji().equals(emoji)) {
                // Same emoji → remove
                commentReactionRepository.delete(reaction);
            } else {
                // Switch emoji
                reaction.setEmoji(emoji);
            }

        } else {
            CommentReaction reaction = new CommentReaction();
            reaction.setComment(comment);
            reaction.setUserId(userId);
            reaction.setEmoji(emoji);
            commentReactionRepository.save(reaction);
        }
    }

    @Transactional
    public void updateComment(UUID commentId, UUID userId, String content) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        comment.setContent(content);
        comment.setEdited(true);
        comment.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        if (comment.isDeleted()) {
            return; // already deleted, do nothing
        }

        comment.setDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
    }
}

