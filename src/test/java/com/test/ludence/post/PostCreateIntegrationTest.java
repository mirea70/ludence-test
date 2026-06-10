package com.test.ludence.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.post.dto.request.PostUpdateRequest;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.support.IntegrationTestSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("포스트 생성 API 통합 테스트")
class PostCreateIntegrationTest extends IntegrationTestSupport {

    private static final byte[] PNG_IMAGE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01
    };

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostHeartCountRepository postHeartCountRepository;

    @Value("${storage.image-directory}")
    private String imageDirectory;

    private Long createdPostId;
    private String createdImageKey;

    @AfterEach
    void deleteCreatedImage() throws Exception {
        if (createdPostId != null) {
            Files.deleteIfExists(Path.of(imageDirectory).resolve(createdImageKey));
        }
    }

    @Test
    @DisplayName("인증된 회원이 PNG multipart를 업로드하면 포스트와 하트 수 및 원본 파일을 저장한다")
    void createsPostHeartCountAndOriginalImage_whenRequestIsValid() throws Exception {
        // given
        String token = signupAndGetToken();
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", PNG_IMAGE);

        // when
        MvcResult result = mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", "title")
                        .param("description", "description")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        createdPostId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // then
        assertThat(postRepository.findById(createdPostId)).isPresent();
        assertThat(postHeartCountRepository.findById(createdPostId)).isPresent();
        assertThat(createdImageKey).matches("[0-9a-f-]{36}\\.png");
        assertThat(createdImageKey).isNotEqualTo(createdPostId + ".png");
        assertThat(Files.readAllBytes(Path.of(imageDirectory).resolve(createdImageKey))).isEqualTo(PNG_IMAGE);

        mockMvc.perform(get("/posts/{id}/image", createdPostId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG_IMAGE));

        mockMvc.perform(get("/posts/{id}", createdPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.id").value(createdPostId))
                .andExpect(jsonPath("$.post.title").value("title"))
                .andExpect(jsonPath("$.post.description").value("description"))
                .andExpect(jsonPath("$.post.username").value("sunny"))
                .andExpect(jsonPath("$.post.heartCount").value(0))
                .andExpect(jsonPath("$.post.hearted").value(false));

        Files.delete(Path.of(imageDirectory).resolve(createdImageKey));
        mockMvc.perform(get("/posts/{id}/image", createdPostId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_009"));
    }

    @Test
    @DisplayName("활성 포스트가 없으면 이미지 조회 시 404를 반환한다")
    void returnsNotFound_whenPostDoesNotExist() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{id}/image", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_008"));

        mockMvc.perform(get("/posts/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_008"));
    }

    @Test
    @DisplayName("작성자가 포스트를 수정하면 메타데이터만 변경되고 이미지 원본은 유지된다")
    void updatesMetadataAndPreservesImage_whenAuthorUpdatesPost() throws Exception {
        // given
        String token = signupAndGetToken("author");
        createdPostId = createPost(token);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        PostUpdateRequest request = new PostUpdateRequest("updated", "changed");

        // when & then
        mockMvc.perform(patch("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPostId));

        mockMvc.perform(get("/posts/{id}", createdPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.title").value("updated"))
                .andExpect(jsonPath("$.post.description").value("changed"));
        assertThat(Files.readAllBytes(Path.of(imageDirectory).resolve(createdImageKey))).isEqualTo(PNG_IMAGE);
    }

    @Test
    @DisplayName("수정 요청의 제목이 null이면 유지하고 설명이 빈 문자열이면 제거한다")
    void preservesTitleAndRemovesDescription_whenPatchValuesRequireIt() throws Exception {
        // given
        String token = signupAndGetToken("author");
        createdPostId = createPost(token);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        PostUpdateRequest request = new PostUpdateRequest(null, "");

        // when & then
        mockMvc.perform(patch("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPostId));

        mockMvc.perform(get("/posts/{id}", createdPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.title").value("title"))
                .andExpect(jsonPath("$.post.description").doesNotExist());
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 포스트를 수정하면 403을 반환한다")
    void returnsForbidden_whenUserIsNotAuthor() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String otherToken = signupAndGetToken("other");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        PostUpdateRequest request = new PostUpdateRequest("updated", null);

        // when & then
        mockMvc.perform(patch("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증 없이 포스트를 수정하면 401을 반환한다")
    void returnsUnauthorized_whenUpdateRequestIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(patch("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostUpdateRequest("updated", null))))
                .andExpect(status().isUnauthorized());
    }

    private String signupAndGetToken() throws Exception {
        return signupAndGetToken("sunny");
    }

    private String signupAndGetToken(String username) throws Exception {
        AuthRequest request = new AuthRequest(username, "password123");
        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private Long createPost(String token) throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", PNG_IMAGE);
        MvcResult result = mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", "title")
                        .param("description", "description")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
