package com.test.ludens.heart.repository;

import com.test.ludens.heart.domain.entity.PostHeartCount;
import java.util.Optional;

public interface PostHeartCountRepositoryCustom {

    long increase(Long postId);

    long decrease(Long postId, long amount);

    Optional<PostHeartCount> findByIdForUpdate(Long postId);
}
