package com.fc.feedservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hidden_posts")
@Data
public class HiddenPost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userId;
    private UUID postId;

    private LocalDateTime hiddenAt = LocalDateTime.now();
}
