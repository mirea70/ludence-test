package com.test.ludence.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.support.JpaTestSupport;
import com.test.ludence.user.domain.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserRepository 테스트")
class UserRepositoryTest extends JpaTestSupport {

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
}
