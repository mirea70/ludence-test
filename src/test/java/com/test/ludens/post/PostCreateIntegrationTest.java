package com.test.ludens.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.ludens.auth.dto.request.AuthRequest;
import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.heart.repository.HeartRepository;
import com.test.ludens.post.dto.request.PostUpdateRequest;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.post.repository.PostViewCountRepository;
import com.test.ludens.support.IntegrationTestSupport;
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

    @Autowired
    private PostViewCountRepository postViewCountRepository;

    @Autowired
    private HeartRepository heartRepository;

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
        assertThat(postViewCountRepository.findById(createdPostId)).isPresent();
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

    @Test
    @DisplayName("작성자가 포스트를 삭제하면 조회를 차단하고 이미지 원본은 보존한다")
    void blocksPostReadsAndPreservesImage_whenAuthorDeletesPost() throws Exception {
        // given
        String token = signupAndGetToken("author");
        createdPostId = createPost(token);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        Path imagePath = Path.of(imageDirectory).resolve(createdImageKey);
        PostHeartCount heartCount = postHeartCountRepository.findById(createdPostId).orElseThrow();
        heartRepository.save(Heart.create(100L, createdPostId));
        heartRepository.save(Heart.create(200L, createdPostId));
        heartCount.increment();
        heartCount.increment();

        // when & then
        mockMvc.perform(delete("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/posts/{id}", createdPostId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/posts/{id}/image", createdPostId))
                .andExpect(status().isNotFound());
        assertThat(heartRepository.count()).isZero();
        assertThat(postHeartCountRepository.findById(createdPostId).orElseThrow().getCount()).isZero();
        assertThat(Files.exists(imagePath)).isTrue();
    }

    @Test
    @DisplayName("인증 없이 포스트를 삭제하면 401을 반환한다")
    void returnsUnauthorized_whenDeleteRequestIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 회원이 활성 포스트에 하트를 추가하면 201을 반환하고 하트 정보를 갱신한다")
    void createsHeartAndUpdatesPostDetail_whenRequestIsAuthenticated() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String viewerToken = signupAndGetToken("viewer");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // when & then
        mockMvc.perform(post("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        mockMvc.perform(get("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.heartCount").value(1))
                .andExpect(jsonPath("$.post.hearted").value(true));

        mockMvc.perform(post("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HEART_003"));
    }

    @Test
    @DisplayName("존재하지 않는 포스트에 하트를 추가하면 404를 반환한다")
    void returnsNotFound_whenHeartPostDoesNotExist() throws Exception {
        // given
        String token = signupAndGetToken("viewer");

        // when & then
        mockMvc.perform(post("/posts/{id}/heart", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HEART_005"));
    }

    @Test
    @DisplayName("인증 없이 포스트에 하트를 추가하면 401을 반환한다")
    void returnsUnauthorized_whenHeartRequestIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(post("/posts/1/heart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 회원이 하트를 삭제하면 204를 반환하고 하트 정보를 갱신한다")
    void deletesHeartAndUpdatesPostDetail_whenHeartExists() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String viewerToken = signupAndGetToken("viewer");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        mockMvc.perform(post("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(delete("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.heartCount").value(0))
                .andExpect(jsonPath("$.post.hearted").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 하트 또는 포스트를 삭제하면 404를 반환한다")
    void returnsNotFound_whenDeletedHeartOrPostDoesNotExist() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String viewerToken = signupAndGetToken("viewer");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // when & then
        mockMvc.perform(delete("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HEART_005"));

        mockMvc.perform(delete("/posts/{id}/heart", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HEART_005"));
    }

    @Test
    @DisplayName("인증 없이 하트를 삭제하면 401을 반환한다")
    void returnsUnauthorized_whenDeleteHeartRequestIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(delete("/posts/1/heart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("포스트 작성자가 하트 회원 목록을 조회하면 페이지 응답을 반환한다")
    void returnsHeartUsers_whenRequesterIsPostAuthor() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String oldToken = signupAndGetToken("alpha_user");
        String newToken = signupAndGetToken("zeta_user");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();
        mockMvc.perform(post("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/posts/{id}/heart", createdPostId)
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/posts/{id}/hearts", createdPostId)
                        .param("page", "1")
                        .param("limit", "1")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.users[0]").value("alpha_user"));
    }

    @Test
    @DisplayName("작성자가 아니거나 포스트가 없으면 하트 회원 목록 조회를 거부한다")
    void rejectsHeartUserQuery_whenRequesterIsNotAuthorOrPostDoesNotExist() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String otherToken = signupAndGetToken("other");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // when & then
        mockMvc.perform(get("/posts/{id}/hearts", createdPostId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_010"));

        mockMvc.perform(get("/posts/{id}/hearts", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_008"));
    }

    @Test
    @DisplayName("인증 없이 하트 회원 목록을 조회하면 401을 반환한다")
    void returnsUnauthorized_whenPostHeartUsersRequestIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/1/hearts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("하트 회원 목록의 페이지 값이 유효하지 않으면 400을 반환한다")
    void returnsBadRequest_whenPostHeartUsersPageIsInvalid() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // when & then
        mockMvc.perform(get("/posts/{id}/hearts", createdPostId)
                        .param("page", "0")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 포스트를 삭제하면 403을 반환한다")
    void returnsForbidden_whenNonAuthorDeletesPost() throws Exception {
        // given
        String authorToken = signupAndGetToken("author");
        String otherToken = signupAndGetToken("other");
        createdPostId = createPost(authorToken);
        createdImageKey = postRepository.findById(createdPostId).orElseThrow().getImageKey();

        // when & then
        mockMvc.perform(delete("/posts/{id}", createdPostId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/posts/{id}", createdPostId))
                .andExpect(status().isOk());
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
