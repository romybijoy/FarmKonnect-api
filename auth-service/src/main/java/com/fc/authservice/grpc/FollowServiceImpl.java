package com.fc.authservice.grpc;

import com.authservice.grpc.FollowServiceGrpc;
import com.authservice.grpc.UserIdRequest;
import com.authservice.grpc.UserIdList;
import com.fc.authservice.repository.FollowRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC implementation of the FollowService.
 * This service exposes APIs for fetching the list of users
 * that a given user is following. It acts as a wrapper over
 * the FollowRepository and returns data through gRPC response objects.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FollowServiceImpl extends FollowServiceGrpc.FollowServiceImplBase {

    private final FollowRepository followRepository;

    /**
     * Fetches the list of user IDs that the given user follows.
     *
     * @param request           gRPC request containing the follower's userId
     * @param responseObserver  gRPC response stream used to send back userId list
     */
    @Override
    public void getFollowedUserIds(UserIdRequest request, StreamObserver<UserIdList> responseObserver) {
        log.info("Received gRPC request to fetch followed users for followerId: {}", request.getUserId());
        try {
            // Convert request string userId into UUID
        UUID followerId = UUID.fromString(request.getUserId());

            // Fetch all relationships where this user is the follower
        List<String> followedIds = followRepository
                .findByFollowerId(followerId)
                .stream()
                .map(relationship -> relationship.getFollowingId().toString())
                .collect(Collectors.toList());

            log.debug("Fetched {} followed user IDs for follower {}", followedIds.size(), followerId);

            // Build the response object
        UserIdList response = UserIdList.newBuilder()
                .addAllUserIds(followedIds)
                .build();

            // Send response to client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
            log.info("Successfully sent followed user IDs to client for follower {}", followerId);

        } catch (Exception e) {
            log.error("Error while processing getFollowedUserIds request for userId: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }
}

