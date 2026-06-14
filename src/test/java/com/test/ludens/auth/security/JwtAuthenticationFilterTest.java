package com.test.ludens.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludens.auth.security.dto.AuthenticatedUser;
import com.test.ludens.auth.security.error.RestAuthenticationEntryPoint;
import com.test.ludens.auth.security.filter.JwtAuthenticationFilter;
import com.test.ludens.auth.security.provider.JwtTokenProvider;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("JwtAuthenticationFilter 테스트")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 활성 회원 ID를 SecurityContext에 저장한다")
    void storesUserIdInSecurityContext_whenBearerTokenIsValid() throws Exception {
        // given
        User user = User.create("sunny", "encoded-password", Instant.parse("2026-06-09T10:00:00Z"));
        given(jwtTokenProvider.getUserId("jwt-token")).willReturn(1L);
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(user));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtTokenProvider,
                userRepository,
                new RestAuthenticationEntryPoint(
                        new ObjectMapper(),
                        Clock.fixed(Instant.parse("2026-06-13T10:00:00Z"), ZoneOffset.UTC)
                )
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");

        // when
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new AuthenticatedUser(1L));
    }
}
