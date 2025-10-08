package com.fc.feedservice.client;

import com.fc.feedservice.dto.UserDto;
import com.userproto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceClient {

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;


    public UserDto getUserById(UUID userId) {
        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserResponse response = userStub.getUserById(request);

        return UserDto.builder()
                .userId(UUID.fromString(response.getUserId()))
                .name(response.getUserName())
                .description(response.getDescription())
                .profileImage(response.getImage())
                .district(response.getDistrict())
                .build();
    }
}
