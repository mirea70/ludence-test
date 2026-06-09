package com.test.ludence.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("AuthController 테스트")
class AuthControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("유효한 회원가입 요청이면 201과 JWT를 반환한다")
    void returnsCreatedAndToken_whenSignupRequestIsValid() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "password123");
        given(authService.signup(request)).willReturn("jwt-token");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("유효한 로그인 요청이면 200과 JWT를 반환한다")
    void returnsOkAndToken_whenLoginRequestIsValid() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "password123");
        given(authService.login(request)).willReturn("jwt-token");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("비밀번호가 8자보다 짧으면 400을 반환한다")
    void returnsBadRequest_whenPasswordIsTooShort() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "short");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }
}
