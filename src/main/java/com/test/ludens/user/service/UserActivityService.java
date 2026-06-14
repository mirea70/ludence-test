package com.test.ludens.user.service;

import com.test.ludens.recommendation.service.RecommendationRefreshService;
import com.test.ludens.user.repository.UserPostViewRepository;
import com.test.ludens.user.repository.UserRepository;
import com.test.ludens.user.repository.UserSearchKeywordRepository;
import java.time.Clock;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserPostViewRepository userPostViewRepository;
    private final UserSearchKeywordRepository userSearchKeywordRepository;
    private final UserRepository userRepository;
    private final RecommendationRefreshService recommendationRefreshService;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPostView(Long postId, Long userId) {
        if (userRepository.findByIdAndDeletedAtIsNull(userId).isEmpty()) {
            return;
        }
        Instant viewedAt = clock.instant();
        userPostViewRepository.upsert(userId, postId, viewedAt);
        recommendationRefreshService.requestRefresh(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(Long userId, String query) {
        if (userRepository.findByIdAndDeletedAtIsNull(userId).isEmpty()) {
            return;
        }
        Instant searchedAt = clock.instant();
        userSearchKeywordRepository.upsert(userId, query, searchedAt);
        recommendationRefreshService.requestRefresh(userId);
    }
}
