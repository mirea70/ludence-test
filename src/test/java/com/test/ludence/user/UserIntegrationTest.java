package com.test.ludence.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("회원 API 통합 테스트")
class UserIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("가입한 회원을 조회하면 포스트 수가 0인 회원 정보를 반환한다")
    void returnsUserWithZeroPostCount_whenSignedUpUserIsQueried() throws Exception {
        // given
        AuthRequest request = new AuthRequest("sunny", "password123");
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/users/sunny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("sunny"))
                .andExpect(jsonPath("$.user.postCount").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 404를 반환한다")
    void returnsNotFound_whenUserDoesNotExist() throws Exception {
        // when & then
        mockMvc.perform(get("/users/unknown"))
                .andExpect(status().isNotFound());
    }
}
