package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.dto.response.UserDetailResponse;
import java.util.Optional;

public interface UserRepositoryCustom {

    Optional<User> findActiveByIdForUpdate(Long id);

    Optional<UserDetailResponse> findActiveDetailByUsername(String username);

    Optional<Long> findActiveIdByUsername(String username);
}
