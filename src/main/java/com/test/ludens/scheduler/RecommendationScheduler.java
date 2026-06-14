package com.test.ludens.scheduler;

import com.test.ludens.recommendation.repository.RecommendationStateRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;

import com.test.ludens.recommendation.service.RecommendationCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationScheduler {

    private static final int USER_BATCH_SIZE = 100;
    private static final int ACTIVITY_RETENTION_DAYS = 7;

    private final RecommendationCalculationService calculationService;
    private final RecommendationStateRepository recommendationStateRepository;
    private final Clock clock;

    @Scheduled(fixedDelay = 120_000, initialDelay = 120_000)
    public void calculateRecommendations() {
        calculationService.calculateCommonRecommendations();
        getUsersToCalculate().forEach(this::calculateUserRecommendations);
    }

    private LinkedHashSet<Long> getUsersToCalculate() {
        Instant activitySince = clock.instant().minus(ACTIVITY_RETENTION_DAYS, ChronoUnit.DAYS);
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        addUsers(userIds, recommendationStateRepository.findPendingUserIds(USER_BATCH_SIZE));
        addUsers(userIds, recommendationStateRepository.findUserIdsAffectedByHeartedAuthors(USER_BATCH_SIZE));
        addUsers(userIds, recommendationStateRepository.findUserIdsAffectedByViewedAuthors(
                activitySince,
                USER_BATCH_SIZE
        ));
        return userIds;
    }

    private void addUsers(LinkedHashSet<Long> userIds, List<Long> additions) {
        for (Long userId : additions) {
            if (userIds.size() >= USER_BATCH_SIZE) {
                return;
            }
            userIds.add(userId);
        }
    }

    private void calculateUserRecommendations(Long userId) {
        try {
            calculationService.calculateUserRecommendations(userId);
        } catch (RuntimeException exception) {
            log.error("Failed to calculate recommendations for userId={}", userId, exception);
        }
    }
}
