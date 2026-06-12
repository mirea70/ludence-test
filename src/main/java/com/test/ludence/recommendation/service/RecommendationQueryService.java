package com.test.ludence.recommendation.service;

import com.test.ludence.common.page.PageRequest;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.recommendation.dto.response.RecommendationResponse;
import com.test.ludence.recommendation.repository.RecommendationQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationQueryService {

    private final RecommendationQueryRepository recommendationQueryRepository;

    public RecommendationResponse getRecommendations(int limit, Long currentUserId) {
        new PageRequest(PageRequest.DEFAULT_PAGE, limit);
        if (currentUserId == null) {
            return getCommonRecommendations(limit);
        }
        return getUserRecommendations(limit, currentUserId);
    }

    private RecommendationResponse getCommonRecommendations(int limit) {
        List<PostDetailResponse> posts = recommendationQueryRepository.findCommon(null, limit);
        long total = recommendationQueryRepository.countActiveCommon();
        return new RecommendationResponse(limit, total, posts);
    }

    private RecommendationResponse getUserRecommendations(int limit, Long userId) {
        List<PostDetailResponse> posts = recommendationQueryRepository.findByUserId(userId, limit);
        long total = recommendationQueryRepository.countActiveByUserId(userId);
        return new RecommendationResponse(limit, total, posts);
    }
}
