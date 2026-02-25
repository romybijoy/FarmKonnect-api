package com.fc.postservice.controller;


import com.fc.postservice.dto.AppealRequest;
import com.fc.postservice.model.Appeal;
import com.fc.postservice.service.AppealService;
import com.fc.postservice.enums.AppealStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Appeal Controller
 *
 * @author Romyb
 * @since 23/02/2026
 */

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appeal Management", description = "APIs for handling user appeals and admin review")
public class AppealController {

    private final AppealService appealService;

    // ------------------------------------------------------------
    // USER → CREATE APPEAL
    // ------------------------------------------------------------
    @PostMapping
    @Operation(
            summary = "Create Appeal",
            description = "Allows post owner to submit an appeal for a removed post"
    )
    @ApiResponse(responseCode = "200", description = "Appeal submitted successfully")
    public ResponseEntity<Appeal> createAppeal(
            @RequestBody AppealRequest request
    ) {

        log.info("API: Create Appeal | postId={}, userId={}", request.getPostId(), request.getUserId());

        Appeal appeal = appealService.createAppeal(request.getPostId(), request.getUserId(),request.getReason());

        return ResponseEntity.ok(appeal);
    }


    // ------------------------------------------------------------
    // USER → GET OWN APPEALS
    // ------------------------------------------------------------
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get User Appeals",
            description = "Fetch appeals submitted by a specific user"
    )
    public ResponseEntity<Page<Appeal>> getUserAppeals(
            @PathVariable UUID userId,
            Pageable pageable
    ) {

        log.info("API: Fetch User Appeals | userId={}", userId);

        Page<Appeal> appeals =
                appealService.getAppealsByUser(userId, pageable);

        return ResponseEntity.ok(appeals);
    }
}
