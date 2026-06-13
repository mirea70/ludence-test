package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSearchKeywordRepositoryCustom {

    void upsert(Long userId, String keyword, Instant searchedAt);

    Optional<UserSearchKeyword> findByUserIdAndKeywordForUpdate(Long userId, String keyword);

    List<UserSearchKeywordId> findIdsLastSearchedBefore(Instant expiredAt, int limit);
}
