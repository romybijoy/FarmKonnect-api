package com.fc.postservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDTO {

    private UUID id;

    private String content;

    private String image;

    private List<String> postImages;

    private String userName;

    private String district;
    private String description;
    private LocalDateTime createdAt;
    private UUID userId;

    private boolean repost;
    private UUID repostedBy;
    private String repostedByName;
    private String repostedByImage;
    private LocalDateTime repostedAt;
    private UUID originalPostId;

    // ✅ Nested Original Post
    private PostDTO originalPost;
}
