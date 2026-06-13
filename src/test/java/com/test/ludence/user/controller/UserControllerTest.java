package com.test.ludence.user.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.security.dto.AuthenticatedUser;
import com.test.ludence.support.ControllerTestSupport;
import com.test.ludence.user.dto.response.UserDetailResponse;
import com.test.ludence.user.dto.response.UserResponse;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostPageResponse;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Test
    @DisplayName("회원 게시글을 조회하면 페이지 정보와 게시글 목록을 반환한다")
    void returnsPostPage_whenUserPostsAreQueried() throws Exception {
        // given
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        PostDetailResponse post = new PostDetailResponse(
                1L, "title", "description", createdAt, createdAt, "sunny", 3L, true
        );
        given(userPostQueryService.getUserPosts("sunny", 2, 10, 7L))
                .willReturn(new PostPageResponse(2, 10, 11L, List.of(post)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthenticatedUser(7L), null)
        );

        // when & then
        mockMvc.perform(get("/users/sunny/posts").param("page", "2").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(11))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));
        verify(userPostQueryService).getUserPosts("sunny", 2, 10, 7L);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("페이지 조건을 생략하면 기본값으로 회원 게시글을 조회한다")
    void usesDefaultPageRequest_whenPageParametersAreOmitted() throws Exception {
        // given
        given(userPostQueryService.getUserPosts("sunny", 1, 20, null))
                .willReturn(new PostPageResponse(1, 20, 0L, List.of()));

        // when & then
        mockMvc.perform(get("/users/sunny/posts"))
                .andExpect(status().isOk());
        verify(userPostQueryService).getUserPosts("sunny", 1, 20, null);
    }

    @Test
    @DisplayName("본인이 하트한 포스트를 조회하면 200과 페이지 응답을 반환한다")
    void returnsPostPage_whenUserHeartsAreQueried() throws Exception {
        // given
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        PostDetailResponse post = new PostDetailResponse(
                1L, "title", null, createdAt, createdAt, "author", 3L, true
        );
        given(userHeartQueryService.getUserHearts(7L, "sunny", 2, 10))
                .willReturn(new PostPageResponse(2, 10, 11L, List.of(post)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthenticatedUser(7L), null)
        );

        // when & then
        mockMvc.perform(get("/users/sunny/hearts").param("page", "2").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(11))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));
        verify(userHeartQueryService).getUserHearts(7L, "sunny", 2, 10);
        SecurityContextHolder.clearContext();
    }
}
