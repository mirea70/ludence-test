package com.test.ludens.user.service;

import com.test.ludens.user.domain.vo.UserPostViewId;
import com.test.ludens.user.domain.vo.UserSearchKeywordId;
import com.test.ludens.user.repository.UserPostViewRepository;
import com.test.ludens.user.repository.UserSearchKeywordRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityCleanupService {

    private static final int RETENTION_DAYS = 7;

    private final UserPostViewRepository userPostViewRepository;
    private final UserSearchKeywordRepository userSearchKeywordRepository;
    private final Clock clock;

    @Transactional
    public void cleanup() {
        Instant expiredAt = clock.instant().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        deletePostViews(expiredAt);
        deleteSearchKeywords(expiredAt);
    }

    private void deletePostViews(Instant expiredAt) {
        List<UserPostViewId> expiredIds = userPostViewRepository.findIdsLastViewedBefore(expiredAt);
        userPostViewRepository.deleteAllByIdInBatch(expiredIds);
    }

    private void deleteSearchKeywords(Instant expiredAt) {
        List<UserSearchKeywordId> expiredIds = userSearchKeywordRepository.findIdsLastSearchedBefore(expiredAt);
        userSearchKeywordRepository.deleteAllByIdInBatch(expiredIds);
    }
}
