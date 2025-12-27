package com.fc.feedservice.client;

import com.fc.feedservice.dto.UserDto;
import com.userproto.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
}
