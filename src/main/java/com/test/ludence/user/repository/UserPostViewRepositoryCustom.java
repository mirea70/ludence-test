package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.vo.UserPostViewId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserPostViewRepositoryCustom {

    void upsert(Long userId, Long postId, Instant viewedAt);

    Optional<UserPostView> findByUserIdAndPostIdForUpdate(Long userId, Long postId);

    List<UserPostViewId> findIdsLastViewedBefore(Instant expiredAt, int limit);
}
