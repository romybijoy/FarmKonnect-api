package com.fc.postservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Reaction Dto
 *
 * @author Romyb
 * @since 20/02/2026
 */
@Getter
@Setter
@Builder
public class ReactionDto {

    private String emoji;
    private long count;
    private boolean reactedByCurrentUser;
}