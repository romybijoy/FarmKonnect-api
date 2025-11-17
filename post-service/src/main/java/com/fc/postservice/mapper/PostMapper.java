package com.fc.postservice.mapper;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.model.Post;

public class PostMapper {

    private PostMapper() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    public static PostDTO mapToDto(Post post) {

        if (post == null) {
            return null;
        }

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setPostImages(post.getPostImages());
        dto.setImage(post.getImage());
        dto.setUserName(post.getUserName());
        dto.setDistrict(post.getDistrict());
        dto.setDescription(post.getDescription());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUserId(post.getUserId());

        // handle repost info
        dto.setRepost(post.isRepost());
        dto.setOriginalPostId(post.getOriginalPostId());
        dto.setRepostedBy(post.getRepostedBy());
        dto.setRepostedAt(post.getRepostedAt());

        return dto;
    }
}
