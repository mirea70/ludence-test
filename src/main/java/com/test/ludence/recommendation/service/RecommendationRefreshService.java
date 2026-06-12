package com.test.ludence.recommendation.service;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationRefreshService {

    private final RecommendationStateRepository recommendationStateRepository;

    @Transactional
    public void requestRefresh(Long userId) {
        if (recommendationStateRepository.incrementRequestedVersion(userId) != 1) {
            throw new IllegalStateException("추천 갱신 상태를 찾을 수 없습니다.");
        }
    }
}
