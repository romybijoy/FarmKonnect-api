package com.fc.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name= "post")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 1000)
    private String content;

    private String postImage;

    private String image;

    private String userName;

    private String district;
    private String description;
    private LocalDateTime createdAt;
    private UUID userId;
    private List<Long> commentIds;


}
