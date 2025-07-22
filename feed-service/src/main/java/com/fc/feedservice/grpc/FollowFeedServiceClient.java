//package com.fc.feedservice.grpc;
//
//import com.followservice.FollowFeedServiceGrpc;
//import com.followservice.FollowRequest;
//import com.followservice.FollowResponse;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//
//@Service
//public class FollowFeedServiceClient {
//
//    private FollowFeedServiceGrpc.FollowFeedServiceBlockingStub followStub;
//
//    @PostConstruct
//    public void init() {
//        ManagedChannel channel = ManagedChannelBuilder
//                .forAddress("localhost", 6565) // Replace with your FollowService gRPC port
//                .usePlaintext()
//                .build();
//
//        followStub = FollowFeedServiceGrpc.newBlockingStub(channel);
//    }
//
//    public List<String> getFollowedUsers(String userId) {
//        FollowRequest request = FollowRequest.newBuilder()
//                .setUserId(userId)
//                .build();
//
//        FollowResponse response = followStub.getFollowedUsers(request);
//        return response.getUserIdsList(); // This gives List<String>
//    }
//}
