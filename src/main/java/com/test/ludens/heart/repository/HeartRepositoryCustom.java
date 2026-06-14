package com.test.ludens.heart.repository;

import com.test.ludens.common.page.PageRequest;
import java.util.List;

public interface HeartRepositoryCustom {

    List<HeartCountByPostId> getCountsByUserId(Long userId);

    long deleteByUserId(Long userId);

    long deleteByPostId(Long postId);

    long deleteByUserIdAndPostId(Long userId, Long postId);

    List<String> findActiveUsernamesByPostId(Long postId, PageRequest pageRequest);
}
