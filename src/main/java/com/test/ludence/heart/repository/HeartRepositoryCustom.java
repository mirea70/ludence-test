package com.test.ludence.heart.repository;

import com.test.ludence.common.page.PageRequest;
import java.util.List;

public interface HeartRepositoryCustom {

    List<HeartCountByPostId> getCountsByUserId(Long userId);

    long deleteByUserId(Long userId);

    long deleteByPostId(Long postId);

    long deleteByUserIdAndPostId(Long userId, Long postId);

    List<String> findActiveUsernamesByPostId(Long postId, PageRequest pageRequest);
}
