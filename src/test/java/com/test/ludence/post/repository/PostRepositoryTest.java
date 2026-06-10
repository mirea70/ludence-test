package com.test.ludence.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.support.JpaTestSupport;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostRepository 테스트")
class PostRepositoryTest extends JpaTestSupport {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";
    private static final Instant CREATED_AT = Instant.parse("2026-06-10T10:00:00Z");

    @Test
    @DisplayName("활성 포스트 ID로 이미지 키를 조회한다")
    void findsImageKey_whenPostIsActive() {
        // given
        Post post = postRepository.save(Post.create(1L, "title", "description", IMAGE_KEY, CREATED_AT));
        entityManager.flush();
        entityManager.clear();

        // when
        String imageKey = postRepository.findActiveImageKeyById(post.getId()).orElseThrow();

        // then
        assertThat(imageKey).isEqualTo(IMAGE_KEY);
    }

    @Test
    @DisplayName("삭제된 포스트 ID로 이미지 키를 조회할 수 없다")
    void doesNotFindImageKey_whenPostIsDeleted() {
        // given
        Post post = postRepository.save(Post.create(1L, "title", "description", IMAGE_KEY, CREATED_AT));
        post.delete(CREATED_AT.plusSeconds(60));
        entityManager.flush();
        entityManager.clear();

        // when
        boolean found = postRepository.findActiveImageKeyById(post.getId()).isPresent();

        // then
        assertThat(found).isFalse();
    }
}
