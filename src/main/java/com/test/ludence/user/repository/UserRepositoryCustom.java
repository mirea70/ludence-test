package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.User;
import java.util.Optional;

public interface UserRepositoryCustom {

    Optional<User> findActiveByIdForUpdate(Long id);
}
