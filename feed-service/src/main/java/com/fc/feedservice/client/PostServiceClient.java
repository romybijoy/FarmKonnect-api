package com.fc.feedservice.client;

import com.postservice.PostMessage;
import com.postservice.UserIdsRequest;
import com.postservice.PostServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostServiceClient {

    private final PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;

    public PostServiceClient() {
        // Ensure this matches your PostService gRPC host and port
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9095)
                .usePlaintext()
                .build();

        postServiceBlockingStub = PostServiceGrpc.newBlockingStub(channel);
    }

    public List<PostMessage> getPostsByUserIds(List<UUID> userIds) {
        UserIdsRequest request = UserIdsRequest.newBuilder()
                .addAllUserIds(userIds.stream().map(UUID::toString).collect(Collectors.toList()))
                .build();

        return postServiceBlockingStub.getPostsByUserIds(request).getPostsList();
    }
}
