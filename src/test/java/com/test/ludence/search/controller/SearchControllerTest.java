package com.test.ludence.search.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostPageResponse;
import com.test.ludence.support.ControllerTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("SearchController 테스트")
class SearchControllerTest extends ControllerTestSupport {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("검색 조건과 인증 사용자가 있으면 게시글 검색 결과를 반환한다")
    void returnsPostPage_whenSearchConditionAndUserExist() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(7L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        PostDetailResponse post = new PostDetailResponse(
                1L, "spring", "description", createdAt, createdAt, "author", 3L, true
        );
        given(postSearchService.searchPosts("spring", 2, 10, 7L))
                .willReturn(new PostPageResponse(2, 10, 11L, List.of(post)));

        // when & then
        mockMvc.perform(get("/search/posts")
                        .param("q", "spring")
                        .param("page", "2")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(11))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));
        verify(postSearchService).searchPosts("spring", 2, 10, 7L);
    }

    @Test
    @DisplayName("검색 조건을 생략하면 기본 페이지 조건으로 전체 게시글을 검색한다")
    void usesDefaults_whenSearchParametersAreOmitted() throws Exception {
        // given
        given(postSearchService.searchPosts(null, 1, 20, null))
                .willReturn(new PostPageResponse(1, 20, 0L, List.of()));

        // when & then
        mockMvc.perform(get("/search/posts"))
                .andExpect(status().isOk());
        verify(postSearchService).searchPosts(null, 1, 20, null);
    }

    @Test
    @DisplayName("페이지 쿼리가 숫자가 아니면 400을 반환한다")
    void returnsBadRequest_whenPageIsNotNumber() throws Exception {
        // when & then
        mockMvc.perform(get("/search/posts").param("page", "not-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_003"))
                .andExpect(jsonPath("$.path").value("/search/posts"));
    }
}
