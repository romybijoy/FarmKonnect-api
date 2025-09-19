package com.fc.postservice.service;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.model.Save;
import com.fc.postservice.repository.PostRepository;
import com.fc.postservice.repository.SavedPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostSaveService {

    private final PostRepository postRepository;
    private final SavedPostRepository saveRepository;

    @Autowired
    public ModelMapper modelMapper;

    public void savePost(UUID postId, UUID userId) {
        if (!saveRepository.existsByUserIdAndPostId(userId, postId)) {
            Save save = new Save();
            save.setUserId(userId);
            save.setPost(postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found")));
            saveRepository.save(save);
        }
    }

    @Transactional
    public void unsavePost(UUID postId, UUID userId) {
        saveRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public boolean isPostSavedByUser(UUID postId, UUID userId) {
        return saveRepository.existsByUserIdAndPostId(userId, postId);
    }

    public long getSaveCount(UUID postId) {
        return saveRepository.countByPostId(postId);
    }

    public List<PostDTO> getSavedPosts(UUID userId) {
        List<Save> saves = saveRepository.findByUserId(userId);
        return saves.stream()
                .map(save -> modelMapper.map(save.getPost(), PostDTO.class)) // map the actual Post
                .toList();
    }


}

