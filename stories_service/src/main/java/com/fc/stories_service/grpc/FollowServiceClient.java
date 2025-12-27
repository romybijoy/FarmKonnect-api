package com.fc.stories_service.grpc;

import com.fc.authservice.grpc.FollowServiceGrpc;
import com.fc.authservice.grpc.UserIdList;
import com.fc.authservice.grpc.UserIdRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * gRPC client for communicating with the Auth-Service FollowService.
 * Responsible for fetching the list of users that the given user follows.
 */
@Service
@Slf4j
public class FollowServiceClient {

    @GrpcClient("auth-service")
    private FollowServiceGrpc.FollowServiceBlockingStub followServiceStub;

    /**
     * Retrieves a list of user IDs that the given user is following.
     *
     * @param userId ID of the requesting user
     * @return list of followed user IDs
     */
    public List<UUID> getFollowedUserIds(UUID userId) {
        log.info("Requesting followed users for userId={}", userId);

        try {
            // Build gRPC request
            UserIdRequest request = UserIdRequest.newBuilder().setUserId(userId.toString()).build();
            // Call auth-service via gRPC
            UserIdList response = followServiceStub.getFollowedUserIds(request);

            // Convert each user ID from string -> UUID
            List<UUID> result = response.getUserIdsList().stream()
                    .map(UUID::fromString)
                    .toList();

            log.info("Retrieved {} followed users for userId={}", result.size(), userId);
            return result;
        }catch (Exception e) {
            log.error("Failed to fetch followed users from auth-service for userId={}. Error: {}",
                    userId, e.getMessage(), e);

            // Fail-safe: return empty list so stories service still functions
            return List.of();
        }
    }
}