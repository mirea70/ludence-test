package com.test.ludence.recommendation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.security.dto.AuthenticatedUser;
import com.test.ludence.recommendation.dto.response.RecommendationResponse;
import com.test.ludence.support.ControllerTestSupport;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("RecommendationController 테스트")
class RecommendationControllerTest extends ControllerTestSupport {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("limit을 생략하면 비로그인 공통 추천을 최대 20개 조회한다")
    void getsCommonRecommendationsWithDefaultLimit_whenUserIsAnonymous() throws Exception {
        // given
        given(recommendationQueryService.getRecommendations(20, null))
                .willReturn(new RecommendationResponse(20, 0, List.of()));

        // when & then
        mockMvc.perform(get("/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.total").value(0));
        verify(recommendationQueryService).getRecommendations(20, null);
    }

    @Test
    @DisplayName("로그인 사용자는 개인 추천을 조회한다")
    void getsUserRecommendations_whenUserIsAuthenticated() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(7L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
        given(recommendationQueryService.getRecommendations(10, 7L))
                .willReturn(new RecommendationResponse(10, 0, List.of()));

        // when & then
        mockMvc.perform(get("/recommendation").param("limit", "10"))
                .andExpect(status().isOk());
        verify(recommendationQueryService).getRecommendations(10, 7L);
    }
}
