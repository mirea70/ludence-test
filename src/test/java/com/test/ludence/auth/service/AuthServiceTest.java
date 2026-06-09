package com.test.ludence.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.auth.security.JwtTokenProvider;
import com.test.ludence.auth.security.PasswordHasher;
import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AuthService 테스트")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-09T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("유효한 회원가입 요청이면 비밀번호를 해시하고 회원을 저장한 뒤 토큰을 반환한다")
    void returnsToken_whenSignupRequestIsValid() {
        // given
        AuthService authService = new AuthService(userRepository, passwordHasher, jwtTokenProvider, clock);
        AuthRequest request = new AuthRequest("sunny", "password123");
        given(userRepository.existsByUsernameValue("sunny")).willReturn(false);
        given(passwordHasher.hash("password123")).willReturn("encoded-password");
        given(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(jwtTokenProvider.createToken(org.mockito.ArgumentMatchers.any())).willReturn("jwt-token");

        // when
        String token = authService.signup(request);

        // then
        assertThat(token).isEqualTo("jwt-token");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(clock.instant());
    }

    @Test
    @DisplayName("중복 username으로 회원가입하면 BusinessException이 발생한다")
    void throwsBusinessException_whenUsernameIsDuplicated() {
        // given
        AuthService authService = new AuthService(userRepository, passwordHasher, jwtTokenProvider, clock);
        given(userRepository.existsByUsernameValue("sunny")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(new AuthRequest("sunny", "password123")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하면 로그인 토큰을 반환한다")
    void returnsToken_whenLoginCredentialsMatch() {
        // given
        AuthService authService = new AuthService(userRepository, passwordHasher, jwtTokenProvider, clock);
        User user = User.create("sunny", "encoded-password", clock.instant());
        given(userRepository.findByUsernameValueAndDeletedAtIsNull("sunny")).willReturn(Optional.of(user));
        given(passwordHasher.matches("password123", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.createToken(user)).willReturn("jwt-token");

        // when
        String token = authService.login(new AuthRequest("sunny", "password123"));

        // then
        assertThat(token).isEqualTo("jwt-token");
    }
}
