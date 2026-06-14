package com.test.ludens.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.support.JpaTestSupport;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.dto.response.UserDetailResponse;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserRepository 테스트")
class UserRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("활성 회원의 username으로 회원 ID를 조회한다")
    void findsActiveUserIdByUsername_whenUserIsActive() {
        // given
        User user = userRepository.save(User.create(
                "sunny",
                "encoded-password",
                Instant.parse("2026-06-10T10:00:00Z")
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        Long userId = userRepository.findActiveIdByUsername("sunny").orElseThrow();

        // then
        assertThat(userId).isEqualTo(user.getId());
    }

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Test
    @DisplayName("username이 저장되어 있으면 존재 여부를 확인할 수 있다")
    void returnsTrue_whenUsernameExists() {
        // given
        userRepository.save(User.create("sunny", "encoded-password", Instant.parse("2026-06-09T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // when
        boolean exists = userRepository.existsByUsernameValue("sunny");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("활성 회원은 username으로 조회할 수 있다")
    void findsActiveUser_whenUsernameMatches() {
        // given
        userRepository.save(User.create("sunny", "encoded-password", Instant.parse("2026-06-09T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // when
        boolean found = userRepository.findByUsernameValueAndDeletedAtIsNull("sunny").isPresent();

        // then
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("활성 회원을 ID로 잠금 조회한다")
    void findsActiveUserForUpdate_whenIdMatches() {
        // given
        User user = userRepository.save(
                User.create("sunny", "encoded-password", Instant.parse("2026-06-09T10:00:00Z"))
        );
        entityManager.flush();
        entityManager.clear();

        // when
        boolean found = userRepository.findActiveByIdForUpdate(user.getId()).isPresent();

        // then
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("활성 회원 상세 조회 시 삭제되지 않은 포스트 수만 집계한다")
    void findsActiveUserDetailWithActivePostCount() {
        // given
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
        User user = userRepository.save(User.create("sunny", "encoded-password", createdAt));
        postRepository.save(Post.create(user.getId(), "active", "description", IMAGE_KEY, createdAt));
        Post deletedPost = postRepository.save(Post.create(
                user.getId(), "deleted", "description", "550e8400-e29b-41d4-a716-446655440001.png", createdAt
        ));
        deletedPost.delete(createdAt.plusSeconds(60));
        entityManager.flush();
        entityManager.clear();

        // when
        UserDetailResponse detail = userRepository.findActiveDetailByUsername("sunny").orElseThrow();

        // then
        assertThat(detail.username()).isEqualTo("sunny");
        assertThat(detail.postCount()).isEqualTo(1);
        assertThat(detail.createdAt()).isEqualTo(createdAt);
    }
}
