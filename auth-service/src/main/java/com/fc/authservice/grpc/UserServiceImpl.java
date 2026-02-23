package com.fc.authservice.grpc;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.UserRepository;
import com.userproto.UserIdsRequest;
import com.userproto.UserRequest;
import com.userproto.UserResponse;
import com.userproto.UserServiceGrpc.UserServiceImplBase;
import com.userproto.UsersResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * gRPC service for fetching user details.
 * This service exposes an endpoint to retrieve a User by ID using gRPC.
 * It interacts with UserRepository to fetch user details and returns
 * a serialized UserResponse via gRPC.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserServiceImpl extends UserServiceImplBase {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves a user from the database based on the provided userId
     * and sends the details back through a gRPC response.
     *
     * @param request          gRPC request containing the userId
     * @param responseObserver gRPC response observer used to send the response
     */
    @Override
    public void getUserById(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("Received request for user details. userId={}", request.getUserId());

        try {
            // Convert the userId string into UUID
            UUID userId = UUID.fromString(request.getUserId());

            // Fetch user from repository
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found for userId={}", request.getUserId());
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("User not found with Id: " + request.getUserId())
                        .asRuntimeException());
                return;
            }

            // Build the gRPC response
            UserResponse response = UserResponse.newBuilder()
                    .setUserId(user.getId().toString())
                    .setUserName(user.getUserName() != null ? user.getUserName() : "")
                    .setImage(user.getImage() != null ? user.getImage() : "")
                    .setDescription(user.getDescription() != null ? user.getDescription() : "")
                    .setDistrict(user.getDistrict() != null ? user.getDistrict() : "")
                    .build();

            log.debug("Sending user response for userId={}", userId);

            // Send response to client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully sent user details for userId={}", userId);

        } catch (Exception e) {
            log.error("Internal error while fetching userId={}: {}", request.getUserId(), e.getMessage(), e);

            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getUsersByIds(UserIdsRequest request,
                              StreamObserver<UsersResponse> responseObserver) {

        log.info("Received batch user request. count={}",
                request.getUserIdsCount());

        try {

            // Convert string IDs → UUID
            List<UUID> ids = request.getUserIdsList()
                    .stream()
                    .map(UUID::fromString)
                    .toList();

            // Fetch all users in single DB query
            List<User> users = userRepository.findAllById(ids);

            // Convert to gRPC responses
            List<UserResponse> responses = users.stream()
                    .map(user -> UserResponse.newBuilder()
                            .setUserId(user.getId().toString())
                            .setUserName(user.getUserName() != null ? user.getUserName() : "")
                            .setImage(user.getImage() != null ? user.getImage() : "")
                            .setDescription(user.getDescription() != null ? user.getDescription() : "")
                            .setDistrict(user.getDistrict() != null ? user.getDistrict() : "")
                            .build()
                    )
                    .toList();

            UsersResponse reply = UsersResponse.newBuilder()
                    .addAllUsers(responses)
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();

            log.info("Successfully returned {} users", responses.size());

        } catch (Exception e) {

            log.error("Error in batch user fetch", e);

            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to fetch users")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }


}

