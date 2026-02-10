package com.fc.authservice.controller;

import com.fc.authservice.dto.UserSuggestionDto;
import com.fc.authservice.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsible for providing user suggestions based on  district relevance and mutual follow relationships.
 * Suggestions prioritize:
 * 1. Users from the same district as the current user.
 * 2. Users followed by people the current user already follows.
 * This endpoint helps improve discoverability by recommending nearby or socially connected farmers.
 * @author Romy Rose Jimmy
 * @since 10/12/2025
 */

@RestController
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping("/suggestions")
    public List<UserSuggestionDto> getSuggestions(
            @RequestParam UUID meId,
            @RequestParam(defaultValue = "5") int size
    ) {
        return suggestionService.getSuggestions(meId, size);
    }
}
