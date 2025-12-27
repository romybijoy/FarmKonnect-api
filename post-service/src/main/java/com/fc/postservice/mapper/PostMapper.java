package com.fc.postservice.mapper;

import com.fc.postservice.dto.PostDTO;
import com.fc.postservice.model.Post;

/**
 * Utility mapper class for converting Post entities into PostDTO objects.
 * This class contains only static methods and cannot be instantiated.
 * Responsibilities:
 *  - Convert core post fields
 *  - Include user metadata stored in the Post entity
 *  - Map repost-related fields
 * Note:
 *  - This mapper does NOT enrich repost metadata such as repostedByName or repostedByImage.
 *    Those fields are expected to be filled in the service layer when performing a repost.
 */
public class PostMapper {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PostMapper() {

        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**
     * Maps a Post entity to a PostDTO.
     *
     * @param post the Post entity to map
     * @return PostDTO or null if input is null
     */
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

        // Repost metadata
        dto.setRepost(post.isRepost());
        dto.setOriginalPostId(post.getOriginalPostId());
        dto.setRepostedBy(post.getRepostedBy());
        dto.setRepostedAt(post.getRepostedAt());

        return dto;
    }
}
