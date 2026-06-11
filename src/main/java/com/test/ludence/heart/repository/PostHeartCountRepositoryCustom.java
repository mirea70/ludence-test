package com.test.ludence.heart.repository;

import com.test.ludence.heart.domain.entity.PostHeartCount;
import java.util.Optional;

public interface PostHeartCountRepositoryCustom {

    long decrease(Long postId, long amount);

    Optional<PostHeartCount> findByIdForUpdate(Long postId);
}
