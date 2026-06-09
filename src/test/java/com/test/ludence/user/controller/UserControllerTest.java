package com.test.ludence.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.support.ControllerTestSupport;
import com.test.ludence.user.dto.response.UserDetailResponse;
import com.test.ludence.user.dto.response.UserResponse;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserController 테스트")
class UserControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("활성 회원을 조회하면 200과 회원 정보를 반환한다")
    void returnsOkAndUser_whenActiveUserExists() throws Exception {
        // given
        UserDetailResponse detail = new UserDetailResponse(
                "sunny",
                2L,
                Instant.parse("2026-06-09T10:00:00Z")
        );
        given(userService.getUser("sunny")).willReturn(new UserResponse(detail));

        // when & then
        mockMvc.perform(get("/users/sunny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("sunny"))
                .andExpect(jsonPath("$.user.postCount").value(2))
                .andExpect(jsonPath("$.user.createdAt").value("2026-06-09T10:00:00Z"));
    }
}
