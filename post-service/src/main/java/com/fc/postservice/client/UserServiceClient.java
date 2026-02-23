package com.fc.postservice.client;

import com.fc.postservice.dto.UserDto;
import com.userproto.UserIdsRequest;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import com.userproto.UsersResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC client for communicating with the Auth Service's UserService.
 * This client retrieves user details through gRPC and maps the
 * response to a UserDto used by the Feed Service.
 */
@Slf4j
@Service
public class UserServiceClient {

    /**
     * Injects a blocking gRPC stub for calling UserService in auth-service.
     * The service name must match configuration in application.yml
     * under grpc.client.auth-service.
     */
    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    /**
     * Fetches a user from auth-service by their ID.
     *
     * @param userId UUID of the user to retrieve
     * @return UserDto containing profile info needed by feed-service
     */
    public UserDto getUserById(UUID userId) {
        log.debug("Requesting user details for userId={}", userId);

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        // gRPC blocking call
        UserResponse response = userStub.getUserById(request);

        log.debug("Retrieved user details from auth-service for userId={}", response.getUserId());

        // Map gRPC response to UserDto
        return UserDto.builder()
                .userId(UUID.fromString(response.getUserId()))
                .name(response.getUserName())
                .description(response.getDescription())
                .profileImage(response.getImage())
                .district(response.getDistrict())
                .build();
    }

    public Map<UUID, UserResponse> getUsersByIds(Set<UUID> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        UserIdsRequest request = UserIdsRequest.newBuilder()
                .addAllUserIds(
                        userIds.stream()
                                .map(UUID::toString)
                                .toList()
                )
                .build();

        UsersResponse response = userStub.getUsersByIds(request);

        Map<UUID, UserResponse> userMap =
                response.getUsersList()
                        .stream()
                        .collect(Collectors.toMap(
                                user -> UUID.fromString(user.getUserId()),
                                user -> user
                        ));
        return userMap;
    }
}
