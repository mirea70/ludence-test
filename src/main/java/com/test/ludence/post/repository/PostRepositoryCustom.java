package com.test.ludence.post.repository;

import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.dto.response.PostDetailResponse;
import java.util.Optional;

public interface PostRepositoryCustom {

    long clearAuthorId(Long authorId);

    Optional<String> findActiveImageKeyById(Long postId);

    Optional<PostDetailResponse> findActiveDetailById(Long postId, Long currentUserId);

    Optional<Post> findActiveByIdForUpdate(Long postId);
}
