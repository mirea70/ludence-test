package com.test.ludence.user.service;

import com.test.ludence.recommendation.service.RecommendationRefreshService;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserPostViewId;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import com.test.ludence.user.repository.UserPostViewRepository;
import com.test.ludence.user.repository.UserRepository;
import com.test.ludence.user.repository.UserSearchKeywordRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private static final int POST_VIEW_LIMIT_PER_USER = 100;
    private static final int SEARCH_KEYWORD_LIMIT_PER_USER = 20;

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
        userPostViewRepository.findByUserIdAndPostIdForUpdate(userId, postId)
                .ifPresentOrElse(
                        view -> view.recordView(viewedAt),
                        () -> userPostViewRepository.save(UserPostView.create(userId, postId, viewedAt))
                );
        List<UserPostViewId> overIds = userPostViewRepository.findIdsExceedingLimitByUserId(userId, POST_VIEW_LIMIT_PER_USER);

        userPostViewRepository.deleteAllByIdInBatch(overIds);
        recommendationRefreshService.requestRefresh(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(Long userId, String query) {
        if (userRepository.findByIdAndDeletedAtIsNull(userId).isEmpty()) {
            return;
        }
        Instant searchedAt = clock.instant();
        userSearchKeywordRepository.findByUserIdAndKeywordForUpdate(userId, query)
                .ifPresentOrElse(
                        keyword -> keyword.recordSearch(searchedAt),
                        () -> userSearchKeywordRepository.save(UserSearchKeyword.create(userId, query, searchedAt))
                );
        List<UserSearchKeywordId> overIds = userSearchKeywordRepository.findIdsExceedingLimitByUserId(userId, SEARCH_KEYWORD_LIMIT_PER_USER);

        userSearchKeywordRepository.deleteAllByIdInBatch(overIds);
        recommendationRefreshService.requestRefresh(userId);
    }
}
