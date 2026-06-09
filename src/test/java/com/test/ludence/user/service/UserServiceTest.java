package com.test.ludence.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.user.dto.response.UserDetailResponse;
import com.test.ludence.user.dto.response.UserResponse;
import com.test.ludence.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserService 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("활성 회원을 조회하면 포스트 수를 포함한 회원 응답을 반환한다")
    void returnsUserResponse_whenActiveUserExists() {
        // given
        UserDetailResponse detail = new UserDetailResponse(
                "sunny",
                2L,
                Instant.parse("2026-06-09T10:00:00Z")
        );
        given(userRepository.findActiveDetailByUsername("sunny")).willReturn(Optional.of(detail));
        UserService userService = new UserService(userRepository);

        // when
        UserResponse response = userService.getUser("sunny");

        // then
        assertThat(response.user()).isEqualTo(detail);
    }

    @Test
    @DisplayName("활성 회원이 존재하지 않으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActiveUserDoesNotExist() {
        // given
        given(userRepository.findActiveDetailByUsername("sunny")).willReturn(Optional.empty());
        UserService userService = new UserService(userRepository);

        // when & then
        assertThatThrownBy(() -> userService.getUser("sunny"))
                .isInstanceOf(BusinessException.class);
    }
}
