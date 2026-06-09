package com.test.ludence.user.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Test
    @DisplayName("회원 생성 시 username과 암호화된 비밀번호를 저장하고 활성 상태가 된다")
    void createsActiveUser_whenValuesAreValid() {
        // given
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");

        // when
        User user = User.create("sunny", "$2a$10$encodedPassword", createdAt);

        // then
        assertThat(user.getUsername()).isEqualTo("sunny");
        assertThat(user.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("회원 탈퇴 시 username을 익명화하고 비밀번호를 제거하며 탈퇴 시각을 기록한다")
    void anonymizesUsernameAndDeletesPassword_whenUserWithdraws() {
        // given
        User user = User.create("sunny", "$2a$10$encodedPassword", Instant.parse("2026-06-09T10:00:00Z"));
        Instant deletedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        user.withdraw("deleted_abc123", deletedAt);

        // then
        assertThat(user.getUsername()).isEqualTo("deleted_abc123");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("이미 탈퇴한 회원을 다시 탈퇴시키면 DomainException이 발생한다")
    void throwsDomainException_whenWithdrawnUserWithdrawsAgain() {
        // given
        User user = User.create("sunny", "$2a$10$encodedPassword", Instant.parse("2026-06-09T10:00:00Z"));
        user.withdraw("deleted_abc123", Instant.parse("2026-06-10T10:00:00Z"));

        // when & then
        assertThatThrownBy(() -> user.withdraw(
                "deleted_def456",
                Instant.parse("2026-06-11T10:00:00Z")
        ))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("암호화된 비밀번호가 비어 있으면 DomainException이 발생한다")
    void throwsDomainException_whenEncodedPasswordIsBlank() {
        // when & then
        assertThatThrownBy(() -> User.create("sunny", " ", Instant.parse("2026-06-09T10:00:00Z")))
                .isInstanceOf(DomainException.class);
    }
}
