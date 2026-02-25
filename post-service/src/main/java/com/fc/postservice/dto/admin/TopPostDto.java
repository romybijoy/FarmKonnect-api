package com.fc.postservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * TopPostDto
 *
 * @author Romyb
 * @since 25/02/2026
 */
@Data
@AllArgsConstructor
public class TopPostDto {

    private UUID postId;
    private String authorName;
    private Long likes;
    private Long comments;
    private Long saves;

}