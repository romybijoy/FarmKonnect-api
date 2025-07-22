package com.fc.feedservice.client;

import com.fc.feedservice.dto.UserDto;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceClient {

    private final com.userproto.UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserServiceClient() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();
        this.userStub = UserServiceGrpc.newBlockingStub(channel);
    }

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
