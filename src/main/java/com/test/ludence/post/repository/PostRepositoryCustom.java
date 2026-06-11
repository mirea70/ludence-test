package com.test.ludence.post.repository;

import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.common.page.PageRequest;
import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    long clearAuthorId(Long authorId);

    Optional<String> findActiveImageKeyById(Long postId);

    Optional<PostDetailResponse> findActiveDetailById(Long postId, Long currentUserId);

    List<PostDetailResponse> findActiveDetailsByAuthorId(
            Long authorId,
            String username,
            Long currentUserId,
            PageRequest pageRequest
    );

    long countActiveByAuthorId(Long authorId);

    Optional<Post> findActiveByIdForUpdate(Long postId);
}
