package com.fc.postservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PostRequest {
    private UUID userId;
    private String content;
    private List<String> postImages;


}

