package com.test.ludence.heart.repository;

import java.util.List;

public interface HeartRepositoryCustom {

    List<HeartCountByPostId> getCountsByUserId(Long userId);

    long deleteByUserId(Long userId);

    long deleteByPostId(Long postId);

    long deleteByUserIdAndPostId(Long userId, Long postId);
}
