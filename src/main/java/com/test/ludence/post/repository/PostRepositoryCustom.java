package com.test.ludence.post.repository;

import java.util.Optional;

public interface PostRepositoryCustom {

    long clearAuthorId(Long authorId);

    Optional<String> findActiveImageKeyById(Long postId);
}
