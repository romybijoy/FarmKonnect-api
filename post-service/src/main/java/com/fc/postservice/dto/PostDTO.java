package com.fc.postservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDTO {

    private UUID id;

    private String content;

    private String image;

    private String postImage;

    private String userName;

    private String district;
    private String description;
    private LocalDateTime createdAt;
    private UUID userId;
}
