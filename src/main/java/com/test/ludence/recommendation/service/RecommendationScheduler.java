package com.test.ludence.recommendation.service;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationScheduler {

    private static final int USER_BATCH_SIZE = 100;

    private final RecommendationCalculationService calculationService;
    private final RecommendationStateRepository recommendationStateRepository;

    @Scheduled(fixedDelay = 120_000, initialDelay = 120_000)
    public void calculateRecommendations() {
        calculationService.calculateCommonRecommendations();
        recommendationStateRepository.findPendingUserIds(USER_BATCH_SIZE)
                .forEach(this::calculateUserRecommendations);
    }

    private void calculateUserRecommendations(Long userId) {
        try {
            calculationService.calculateUserRecommendations(userId);
        } catch (RuntimeException exception) {
            log.error("Failed to calculate recommendations for userId={}", userId, exception);
        }
    }
}
