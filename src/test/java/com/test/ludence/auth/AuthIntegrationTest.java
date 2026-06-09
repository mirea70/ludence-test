package com.test.ludence.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("인증 API 통합 테스트")
class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("회원가입 후 동일한 계정으로 로그인하면 각각 JWT를 반환한다")
    void returnsTokens_whenUserSignsUpAndLogsIn() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "password123");
        String content = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
