package com.fc.feedservice.client;

import com.authservice.grpc.UserIdList;
import com.authservice.grpc.UserIdRequest;
import com.authservice.grpc.FollowServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FollowServiceClient {

    private final FollowServiceGrpc.FollowServiceBlockingStub followStub;

    public FollowServiceClient(
            @Value("${follow.grpc.host:localhost}") String host,
            @Value("${follow.grpc.port:6565}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        followStub = FollowServiceGrpc.newBlockingStub(channel);
    }

    public List<UUID> getFollowedUserIds(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserIdList response = followStub.getFollowedUserIds(request);
        return response.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();
    }
}
