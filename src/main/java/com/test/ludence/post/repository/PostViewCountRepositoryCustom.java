package com.test.ludence.post.repository;

import com.test.ludence.post.domain.entity.PostViewCount;
import java.util.Optional;

public interface PostViewCountRepositoryCustom {

    Optional<PostViewCount> findByIdForUpdate(Long postId);
}
