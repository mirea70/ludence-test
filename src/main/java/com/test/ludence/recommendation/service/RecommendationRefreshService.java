package com.test.ludence.recommendation.service;

import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationRefreshService {

    private final RecommendationStateRepository recommendationStateRepository;
    private final UserRepository userRepository;

    @Transactional
    public void requestRefresh(Long userId) {
        if (recommendationStateRepository.incrementRequestedVersion(userId) == 1) {
            return;
        }

        initializeMissingState(userId);
    }

    private void initializeMissingState(Long userId) {
        userRepository.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException("추천 갱신 대상 사용자를 찾을 수 없습니다."));

        if (recommendationStateRepository.incrementRequestedVersion(userId) == 0) {
            recommendationStateRepository.save(RecommendationState.create(userId));
        }
    }
}
