//package com.fc.feedservice.grpc;
//
//import com.postservice.PostListResponse;
//import com.postservice.PostMessage;
//import com.postservice.PostServiceGrpc;
//import com.postservice.UserIdsRequest;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class FeedServiceClient {
//
//    private PostServiceGrpc.PostServiceBlockingStub postServiceBlockingStub;
//
//    @PostConstruct
//    public void init() {
//        ManagedChannel channel = ManagedChannelBuilder
//                .forAddress("localhost", 9090) // Change to your PostService gRPC port
//                .usePlaintext()
//                .build();
//        postServiceBlockingStub = PostServiceGrpc.newBlockingStub(channel);
//    }
//
//    public List<PostMessage> getPostsByUserIds(List<String> userIds) {
//        UserIdsRequest request = UserIdsRequest.newBuilder()
//                .addAllUserIds(userIds)
//                .build();
//
//        PostListResponse response = postServiceBlockingStub.getPostsByUserIds(request);
//        return response.getPostsList();
//    }
//}
