package com.fc.postservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class PostRequest {
    private UUID userId;
    private String content;
    private String postImage;


}

