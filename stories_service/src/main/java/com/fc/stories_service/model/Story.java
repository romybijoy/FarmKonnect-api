package com.fc.stories_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name= "stories")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_name", nullable = false)
    private String username;
    private String email;
    private String profilePic;
    private String imageUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String videoUrl;
//    private String caption;
    private String type; // "image" or "video"
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
//    private String visibility; // "public", "private", etc.
}
