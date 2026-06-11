package com.test.ludence.post.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.request.PostUpdateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostResponse;
import com.test.ludence.support.ControllerTestSupport;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("PostController 테스트")
class PostControllerTest extends ControllerTestSupport {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 multipart 요청이면 포스트를 생성하고 201과 ID를 반환한다")
    void returnsCreatedAndId_whenMultipartRequestIsValid() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", new byte[10]);
        given(postCreateService.createPost(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(PostCreateRequest.class)
        )).willReturn(new PostIdResponse(10L));

        // when & then
        mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", "title")
                        .param("description", "description"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/posts/10"))
                .andExpect(jsonPath("$.id").value(10));
        ArgumentCaptor<PostCreateRequest> requestCaptor = ArgumentCaptor.forClass(PostCreateRequest.class);
        verify(postCreateService).createPost(org.mockito.ArgumentMatchers.eq(1L), requestCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().title()).isEqualTo("title");
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().description()).isEqualTo("description");
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().image()).isSameAs(image);
    }

    @Test
    @DisplayName("이미지가 없는 요청이면 400을 반환한다")
    void returnsBadRequest_whenImageIsMissing() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        // when & then
        mockMvc.perform(multipart("/posts")
                        .param("title", "title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.image").exists());
    }

    @Test
    @DisplayName("포스트 이미지를 조회하면 PNG 원본 바이트를 반환한다")
    void returnsPngOriginalBytes_whenPostImageExists() throws Exception {
        // given
        byte[] image = {(byte) 0x89, 0x50, 0x4E, 0x47};
        given(postImageService.getImage(1L)).willReturn(new ByteArrayResource(image));

        // when & then
        mockMvc.perform(get("/posts/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(image));
        verify(postImageService).getImage(1L);
    }

    @Test
    @DisplayName("익명 사용자가 포스트를 조회하면 상세 정보와 hearted false를 반환한다")
    void returnsPostWithNotHearted_whenUserIsAnonymous() throws Exception {
        // given
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        PostDetailResponse detail = new PostDetailResponse(
                1L, "title", "description", createdAt, createdAt, "author", 3L, false
        );
        given(postQueryService.getPost(1L, null)).willReturn(new PostResponse(detail));

        // when & then
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.id").value(1))
                .andExpect(jsonPath("$.post.username").value("author"))
                .andExpect(jsonPath("$.post.heartCount").value(3))
                .andExpect(jsonPath("$.post.hearted").value(false));
        verify(postQueryService).getPost(1L, null);
    }

    @Test
    @DisplayName("유효한 포스트 수정 요청이면 200과 포스트 ID를 반환한다")
    void returnsOkAndId_whenUpdateRequestIsValid() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
        PostUpdateRequest request = new PostUpdateRequest("updated", "description");
        given(postUpdateService.updatePost(1L, 10L, request)).willReturn(new PostIdResponse(10L));

        // when & then
        mockMvc.perform(patch("/posts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
        verify(postUpdateService).updatePost(1L, 10L, request);
    }

    @Test
    @DisplayName("포스트 수정 요청의 제목이 비어 있으면 변경 없이 200을 반환한다")
    void returnsOk_whenUpdateTitleIsBlank() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        PostUpdateRequest request = new PostUpdateRequest(" ", "description");
        given(postUpdateService.updatePost(1L, 10L, request)).willReturn(new PostIdResponse(10L));

        // when & then
        mockMvc.perform(patch("/posts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("포스트 수정 요청의 제목과 설명이 null이면 200을 반환한다")
    void returnsOk_whenUpdateFieldsAreNull() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
        PostUpdateRequest request = new PostUpdateRequest(null, null);
        given(postUpdateService.updatePost(1L, 10L, request)).willReturn(new PostIdResponse(10L));

        // when & then
        mockMvc.perform(patch("/posts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("인증된 작성자가 포스트를 삭제하면 204를 반환한다")
    void returnsNoContent_whenAuthorDeletesPost() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        // when & then
        mockMvc.perform(delete("/posts/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(postDeleteService).deletePost(1L, 10L);
    }

    @Test
    @DisplayName("인증된 회원이 포스트에 하트를 추가하면 201과 빈 응답을 반환한다")
    void returnsCreatedWithoutBody_whenUserAddsHeart() throws Exception {
        // given
        AuthenticatedUser user = new AuthenticatedUser(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        // when & then
        mockMvc.perform(post("/posts/10/heart"))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
        verify(heartCreateService).createHeart(1L, 10L);
    }
}
