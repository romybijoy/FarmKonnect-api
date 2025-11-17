package com.fc.authservice.grpc;

import com.authservice.grpc.FollowServiceGrpc;
import com.authservice.grpc.UserIdRequest;
import com.authservice.grpc.UserIdList;
import com.fc.authservice.repository.FollowRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class FollowServiceImpl extends FollowServiceGrpc.FollowServiceImplBase {

    private final FollowRepository followRepository;

    @Override
    public void getFollowedUserIds(UserIdRequest request, StreamObserver<UserIdList> responseObserver) {
        UUID followerId = UUID.fromString(request.getUserId());
        List<String> followedIds = followRepository
                .findByFollowerId(followerId)
                .stream()
                .map(relationship -> relationship.getFollowingId().toString())
                .collect(Collectors.toList());

        UserIdList response = UserIdList.newBuilder()
                .addAllUserIds(followedIds)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

