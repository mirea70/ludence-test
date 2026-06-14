package com.test.ludens.user.repository;

import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.dto.response.UserDetailResponse;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {

    Optional<User> findActiveByIdForUpdate(Long id);

    Optional<UserDetailResponse> findActiveDetailByUsername(String username);

    Optional<Long> findActiveIdByUsername(String username);

    List<UserDetailResponse> findAllActiveDetails();
}
