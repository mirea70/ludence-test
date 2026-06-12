package com.test.ludence.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.support.IntegrationTestSupport;
import com.test.ludence.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("인증 API 통합 테스트")
class AuthIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationStateRepository recommendationStateRepository;

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

        Long userId = userRepository.findActiveIdByUsername("sunny").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(recommendationStateRepository.findById(userId)).isPresent();
    }

    @Test
    @DisplayName("Bearer 토큰 없이 회원 탈퇴를 요청하면 401을 반환한다")
    void returnsUnauthorized_whenWithdrawalHasNoBearerToken() throws Exception {
        // when & then
        mockMvc.perform(delete("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 Bearer 토큰으로 회원 탈퇴를 요청하면 표준 에러 형식의 401을 반환한다")
    void returnsUnauthorizedErrorResponse_whenWithdrawalHasInvalidBearerToken() throws Exception {
        // when & then
        mockMvc.perform(delete("/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_003"))
                .andExpect(jsonPath("$.path").value("/auth/me"));
    }

    @Test
    @DisplayName("회원 탈퇴 후 기존 토큰과 계정은 사용할 수 없고 기존 username으로 재가입할 수 있다")
    void invalidatesCredentialsAndAllowsUsernameReuse_whenUserWithdraws() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "password123");
        String content = objectMapper.writeValueAsString(request);
        String signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(signupResponse).get("token").asText();

        // when & then
        mockMvc.perform(delete("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }
}
