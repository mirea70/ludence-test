package com.test.ludens.post.repository;

import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.common.page.PageRequest;
import java.time.Instant;
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

    Optional<PostHeartAccess> findActiveHeartAccessById(Long postId);

    List<PostDetailResponse> findActiveDetailsHeartedByUserId(Long userId, PageRequest pageRequest);

    long countActiveHeartedByUserId(Long userId);

    List<PostDetailResponse> findActiveDetailsByQuery(
            String query,
            Long currentUserId,
            PageRequest pageRequest
    );

    long countActiveByQuery(String query);

    List<PostDetailResponse> findAllActiveDetails();

    List<String> findAllImageKeys();

    List<PostCleanupCandidate> findCleanupCandidates(
            Instant expiredAt,
            Instant cursorDeletedAt,
            Long cursorPostId,
            int limit
    );

    long deleteExpiredPostData(Long postId, Instant expiredAt);
}
