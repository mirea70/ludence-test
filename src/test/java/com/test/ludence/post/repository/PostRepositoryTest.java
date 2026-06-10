package com.test.ludence.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.user.domain.entity.User;
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

    @Test
    @DisplayName("활성 포스트 상세 정보와 현재 사용자의 하트 여부를 조회한다")
    void findsActivePostDetailWithHearted_whenUserHeartedPost() {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        User viewer = userRepository.save(User.create("viewer", "encoded-password", CREATED_AT));
        Post post = postRepository.save(Post.create(author.getId(), "title", "description", IMAGE_KEY, CREATED_AT));
        PostHeartCount heartCount = PostHeartCount.create(post.getId());
        heartCount.increment();
        postHeartCountRepository.save(heartCount);
        heartRepository.save(Heart.create(viewer.getId(), post.getId()));
        entityManager.flush();
        entityManager.clear();

        // when
        PostDetailResponse detail = postRepository.findActiveDetailById(post.getId(), viewer.getId()).orElseThrow();

        // then
        assertThat(detail.id()).isEqualTo(post.getId());
        assertThat(detail.username()).isEqualTo("author");
        assertThat(detail.heartCount()).isEqualTo(1);
        assertThat(detail.hearted()).isTrue();
    }

    @Test
    @DisplayName("익명 사용자가 활성 포스트 상세 정보를 조회하면 hearted는 false다")
    void findsActivePostDetailWithNotHearted_whenUserIsAnonymous() {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        Post post = postRepository.save(Post.create(author.getId(), "title", null, IMAGE_KEY, CREATED_AT));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        entityManager.flush();
        entityManager.clear();

        // when
        PostDetailResponse detail = postRepository.findActiveDetailById(post.getId(), null).orElseThrow();

        // then
        assertThat(detail.description()).isNull();
        assertThat(detail.hearted()).isFalse();
    }

    @Test
    @DisplayName("활성 포스트를 수정용으로 잠금 조회한다")
    void findsActivePostForUpdate_whenPostIsActive() {
        // given
        Post post = postRepository.save(Post.create(1L, "title", null, IMAGE_KEY, CREATED_AT));
        entityManager.flush();
        entityManager.clear();

        // when
        Post found = postRepository.findActiveByIdForUpdate(post.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(post.getId());
    }
}
