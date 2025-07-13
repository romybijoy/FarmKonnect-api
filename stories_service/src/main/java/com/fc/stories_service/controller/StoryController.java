package com.fc.stories_service.controller;

import com.fc.stories_service.dto.StoryDto;
import com.fc.stories_service.dto.StoryResponse;
import com.fc.stories_service.model.Story;
import com.fc.stories_service.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping("/create")
    public ResponseEntity<Story> uploadStory(@RequestBody StoryDto story) {
        return ResponseEntity.ok(storyService.createStory(story));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StoryResponse> getUserStories(@PathVariable UUID userId) {
        return ResponseEntity.ok(storyService.getUserStoriesByUserId(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StoryResponse>> getAllActiveStories() {
        return ResponseEntity.ok(storyService.getAllActiveStories());
    }

//    @GetMapping("/feed")
//    public ResponseEntity<List<StoryResponse>> getStoriesFromFollowings(@PathVariable String email) {
////    String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        List<StoryResponse> stories = storyService.getStoriesOfFollowings(email);
//        return ResponseEntity.ok(stories);
//    }

}