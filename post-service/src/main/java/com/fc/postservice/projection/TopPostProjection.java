package com.fc.postservice.projection;

import java.util.UUID;

/**
 * Top Post Projection
 *
 * @author Romyb
 * @since 25/02/2026
 */
public interface TopPostProjection {

    UUID getPostId();
    String getUserName();
    Long getLikes();
    Long getComments();
    Long getSaves();
}
