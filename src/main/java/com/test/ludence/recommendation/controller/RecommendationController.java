package com.test.ludence.recommendation.controller;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.recommendation.dto.response.RecommendationResponse;
import com.test.ludence.recommendation.service.RecommendationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationQueryService recommendationQueryService;

    @GetMapping("/recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long currentUserId = user == null ? null : user.id();
        return ResponseEntity.ok(recommendationQueryService.getRecommendations(limit, currentUserId));
    }
}
