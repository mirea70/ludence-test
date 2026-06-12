package com.test.ludence.recommendation.service;

import com.test.ludence.recommendation.domain.entity.CommonRecommendation;
import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.recommendation.domain.entity.UserRecommendation;
import com.test.ludence.recommendation.repository.CommonRecommendationRepository;
import com.test.ludence.recommendation.repository.RecommendationCandidateQueryRepository;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.recommendation.repository.UserRecommendationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationCalculationService {

    private static final int CANDIDATE_LIMIT = 40;
    private static final int ACTIVITY_RETENTION_DAYS = 7;

    private final RecommendationCandidateQueryRepository candidateQueryRepository;
    private final CommonRecommendationRepository commonRecommendationRepository;
    private final UserRecommendationRepository userRecommendationRepository;
    private final RecommendationStateRepository recommendationStateRepository;
    private final Clock clock;

    @Transactional
    public void calculateCommonRecommendations() {
        Instant calculatedAt = clock.instant();
        List<Long> postIds = candidateQueryRepository.findCommon(CANDIDATE_LIMIT);
        commonRecommendationRepository.deleteAllInBatch();
        commonRecommendationRepository.saveAll(toCommonRecommendations(postIds, calculatedAt));
    }

    @Transactional
    public void calculateUserRecommendations(Long userId) {
        RecommendationState state = recommendationStateRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException("추천 갱신 상태를 찾을 수 없습니다."));
        long requestedVersion = state.getRequestedVersion();
        Instant calculatedAt = clock.instant();
        Instant since = calculatedAt.minus(ACTIVITY_RETENTION_DAYS, ChronoUnit.DAYS);

        LinkedHashSet<Long> candidates = new LinkedHashSet<>();
        addCandidates(candidates, candidateQueryRepository.findByHeartedAuthors(userId, CANDIDATE_LIMIT));
        addCandidates(candidates, candidateQueryRepository.findByViewedAuthors(userId, since, CANDIDATE_LIMIT));
        addCandidates(candidates, candidateQueryRepository.findByRecentSearches(userId, since, CANDIDATE_LIMIT));
        addCandidates(candidates, candidateQueryRepository.findLatest(userId, CANDIDATE_LIMIT));

        userRecommendationRepository.deleteByUserId(userId);
        userRecommendationRepository.saveAll(toUserRecommendations(userId, candidates, calculatedAt));
        state.completeCalculation(requestedVersion);
    }

    private void addCandidates(LinkedHashSet<Long> candidates, List<Long> additions) {
        for (Long postId : additions) {
            if (candidates.size() >= CANDIDATE_LIMIT) {
                return;
            }
            candidates.add(postId);
        }
    }

    private List<CommonRecommendation> toCommonRecommendations(List<Long> postIds, Instant calculatedAt) {
        List<CommonRecommendation> recommendations = new ArrayList<>();
        for (int index = 0; index < Math.min(postIds.size(), CANDIDATE_LIMIT); index++) {
            recommendations.add(CommonRecommendation.create(postIds.get(index), index + 1, calculatedAt));
        }
        return recommendations;
    }

    private List<UserRecommendation> toUserRecommendations(
            Long userId,
            LinkedHashSet<Long> postIds,
            Instant calculatedAt
    ) {
        List<UserRecommendation> recommendations = new ArrayList<>();
        int rank = 1;
        for (Long postId : postIds) {
            recommendations.add(UserRecommendation.create(userId, postId, rank++, calculatedAt));
        }
        return recommendations;
    }
}
