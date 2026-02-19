package com.fc.postservice.service;

import com.fc.postservice.enums.PostStatus;
import com.fc.postservice.model.Post;
import com.fc.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import com.fc.ai.AIServiceGrpc;
import com.fc.ai.AIResponse;
import com.fc.ai.ContentRequest;
import com.fc.ai.ImageRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Moderation Service for AI
 *
 * @author Romyb
 * @since 17/02/2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final PostRepository postRepository;
    private final ModerationAuditService moderationAuditService;


    @GrpcClient("ai-service")
    private AIServiceGrpc.AIServiceBlockingStub aiStub;


    @Async
    public void validatePostAsync(UUID postId) {

        log.error("AI VALIDATION STARTED for postId={}", postId);
        try {
            Optional<Post> optionalPost = postRepository.findById(postId);
            if (optionalPost.isEmpty()) return;

            Post post = optionalPost.get();

            // -------------------------
            // Content Validation
            // -------------------------
            AIResponse contentResult = callContentAI(
                    post.getContent() == null ? "" : post.getContent()
            );


            boolean contentApproved = "agri".equals(contentResult.getVerdict());
            double maxScore = contentResult.getScore();

            // -------------------------
            // Image Validation
            // -------------------------
            boolean imageApproved = true;

            if (post.getPostImages() != null) {
                for (String imageUrl : post.getPostImages()) {

                    AIResponse imageResult = callImageAI(imageUrl);


                    if (!"agri".equals(imageResult.getVerdict())) {
                        imageApproved = false;
                    }

                    maxScore = Math.max(maxScore, imageResult.getScore());
                }
            }

            // -------------------------
            // Final Decision
            // -------------------------
            if (contentApproved && imageApproved) {

                post.setStatus(PostStatus.ACTIVE);
                post.setModerationReason("Approved by AI");
                post.setAiScore(maxScore);

                moderationAuditService.record(
                        null,
                        post.getId(),
                        null,
                        "AI_APPROVED",
                        "Content and images approved by AI"
                );

            } else {

                post.setStatus(PostStatus.REJECTED);
                post.setModerationReason("AI rejected content or image");
                post.setAiScore(maxScore);

                moderationAuditService.record(
                        null,
                        post.getId(),
                        null,
                        "AI_REJECTED",
                        "Content or image not agriculture related"
                );
            }

            postRepository.save(post);

            log.info("AI moderation completed | postId={} | status={}",
                    postId, post.getStatus());

        } catch (Exception e) {
            log.error("AI moderation failed for postId={}", postId, e);
        }
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "aiFallback")
    public AIResponse callContentAI(String text) {

        log.error("Calling AI content service...");
        return aiStub.validateContent(
                ContentRequest.newBuilder()
                        .setText(text)
                        .build()
        );
    }

    public AIResponse aiFallback(String text, Throwable t) {

        log.error("AI service unavailable. Falling back. Reason: {}", t.getMessage());

        // Safe fallback decision
        return AIResponse.newBuilder()
                .setVerdict("non-agri")
                .setScore(0.0)
                .build();
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "aiImageFallback")
    public AIResponse callImageAI(String imageUrl) {
        return aiStub.validateImage(
                ImageRequest.newBuilder()
                        .setImageUrl(imageUrl)
                        .build()
        );
    }

    public AIResponse aiImageFallback(String imageUrl, Throwable t) {
        log.error("AI image validation failed: {}", t.getMessage());

        return AIResponse.newBuilder()
                .setVerdict("non-agri")
                .setScore(0.0)
                .build();
    }


}
