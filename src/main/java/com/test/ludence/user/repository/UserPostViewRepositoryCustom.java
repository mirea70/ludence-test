package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserPostView;
import java.util.Optional;

public interface UserPostViewRepositoryCustom {

    Optional<UserPostView> findByUserIdAndPostIdForUpdate(Long userId, Long postId);
}
