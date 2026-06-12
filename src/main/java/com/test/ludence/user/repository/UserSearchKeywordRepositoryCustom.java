package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserSearchKeyword;
import java.util.Optional;

public interface UserSearchKeywordRepositoryCustom {

    Optional<UserSearchKeyword> findByUserIdAndKeywordForUpdate(Long userId, String keyword);
}
