package com.fc.postservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PostRequest {
    private UUID userId;
    private String content;
    private String postImage;


}

