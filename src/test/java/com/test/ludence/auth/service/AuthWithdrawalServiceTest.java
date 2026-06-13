package com.test.ludence.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.auth.security.generator.AnonymousUsernameGenerator;
import com.test.ludence.heart.repository.HeartCountByPostId;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AuthWithdrawalService 테스트")
@ExtendWith(MockitoExtension.class)
class AuthWithdrawalServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-09T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Mock
    private AnonymousUsernameGenerator anonymousUsernameGenerator;

    @Test
    @DisplayName("회원 탈퇴 시 작성자 참조와 하트를 제거하고 하트 수를 감소시킨 뒤 회원을 익명화한다")
    void cleansRelatedDataAndAnonymizesUser_whenUserWithdraws() {
        // given
        User user = User.create("sunny", "encoded-password", Instant.parse("2026-06-08T10:00:00Z"));
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(heartRepository.getCountsByUserId(1L)).willReturn(List.of(new HeartCountByPostId(10L, 2L)));
        given(postHeartCountRepository.decrease(10L, 2L)).willReturn(1L);
        given(anonymousUsernameGenerator.generate()).willReturn("deleted_abc123");
        AuthWithdrawalService service = new AuthWithdrawalService(
                userRepository,
                postRepository,
                heartRepository,
                postHeartCountRepository,
                anonymousUsernameGenerator,
                clock
        );

        // when
        service.withdraw(1L);

        // then
        verify(postRepository).clearAuthorId(1L);
        verify(postHeartCountRepository).decrease(10L, 2L);
        verify(heartRepository).deleteByUserId(1L);
        assertThat(user.getUsername()).isEqualTo("deleted_abc123");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getDeletedAt()).isEqualTo(clock.instant());
    }
}
