package com.fc.authservice.grpc;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.UserRepository;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc.UserServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;


@GrpcService
public class UserServiceImpl extends UserServiceImplBase {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void getUserById(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("User not found with Id: " + request.getUserId())
                        .asRuntimeException());
                return;
            }

            UserResponse response = UserResponse.newBuilder()
                    .setUserId(user.getId().toString())
                    .setUserName(user.getUserName() != null ? user.getUserName() : "")
                    .setImage(user.getImage() != null ? user.getImage() : "")
                    .setDescription(user.getDescription() != null ? user.getDescription() : "")
                    .setDistrict(user.getDistrict() != null ? user.getDistrict() : "")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

}

