package com.fc.postservice.repository;

import com.fc.postservice.model.SavedPost;
import com.fc.postservice.model.SavedPostId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedPostRepository  extends JpaRepository<SavedPost, SavedPostId> {

    List<SavedPost> findByIdUserId(UUID userId); // Access composite key fields using `id.fieldName`

//    Optional<SavedPost> findByIdUserIdAndIdPostId(UUID userId, UUID postId);
//
//    void deleteByIdUserIdAndIdPostId(UUID userId, UUID postId);

}
