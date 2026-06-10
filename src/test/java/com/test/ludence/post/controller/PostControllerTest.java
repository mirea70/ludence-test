package com.test.ludence.post.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.support.ControllerTestSupport;
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
}
