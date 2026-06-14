package com.test.ludens.user.repository;

import com.test.ludens.user.domain.entity.UserSearchKeyword;
import com.test.ludens.user.domain.vo.UserSearchKeywordId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSearchKeywordRepositoryCustom {

    void upsert(Long userId, String keyword, Instant searchedAt);

    Optional<UserSearchKeyword> findByUserIdAndKeywordForUpdate(Long userId, String keyword);

    List<UserSearchKeywordId> findIdsLastSearchedBefore(Instant expiredAt);
}
