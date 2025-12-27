package com.fc.feedservice.client;

import com.authservice.grpc.UserIdList;
import com.authservice.grpc.UserIdRequest;
import com.authservice.grpc.FollowServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * gRPC client for communicating with the AuthService FollowService.
 * This client queries the list of user IDs that a particular user is following,
 * which is required by the feed-service to generate personalized feeds.
 */
@Slf4j
@Service
public class FollowServiceClient {

    private final FollowServiceGrpc.FollowServiceBlockingStub followStub;
    private final ManagedChannel channel;

    /**
     * Initializes the gRPC client and creates a blocking stub.
     *
     * @param host gRPC server host (configured via application properties)
     * @param port gRPC server port (configured via application properties)
     */
    public FollowServiceClient(
            @Value("${follow.grpc.host:localhost}") String host,
            @Value("${follow.grpc.port:6565}") int port) {

        log.info("Initializing FollowService gRPC client → {}:{}", host, port);

       this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()// Disable TLS for internal microservice communication
                .build();

        followStub = FollowServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Calls the FollowService gRPC API to fetch the list of users
     * that a given user is following.
     *
     * @param userId ID of the user whose following list is needed
     * @return List of userId values that the user follows
     */
    public List<UUID> getFollowedUserIds(UUID userId) {
        log.debug("Requesting followed user IDs for user {}", userId);

        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        // Synchronous gRPC call
        UserIdList response = followStub.getFollowedUserIds(request);

        log.debug("Received {} followed users for {}", response.getUserIdsCount(), userId);

        return response.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();
    }

    /**
     * Shuts down the gRPC channel gracefully.
     * Recommended to prevent resource leakage.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC channel for FollowServiceClient");
        channel.shutdown();
    }
}
