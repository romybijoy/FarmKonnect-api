package com.fc.stories_service.grpc;

import com.fc.authservice.grpc.FollowServiceGrpc;
import com.fc.authservice.grpc.UserIdList;
import com.fc.authservice.grpc.UserIdRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowServiceClient {

    @GrpcClient("auth-service")
    private FollowServiceGrpc.FollowServiceBlockingStub followServiceStub;

    public List<UUID> getFollowedUserIds(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder().setUserId(userId.toString()).build();
        UserIdList response = followServiceStub.getFollowedUserIds(request);
        return response.getUserIdsList().stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }
}