package com.test.ludence.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.ludence.auth.dto.request.AuthRequest;
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
        assertThat(createdImageKey).doesNotStartWith(createdPostId.toString());
        assertThat(Files.readAllBytes(Path.of(imageDirectory).resolve(createdImageKey))).isEqualTo(PNG_IMAGE);
    }

    private String signupAndGetToken() throws Exception {
        AuthRequest request = new AuthRequest("sunny", "password123");
        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }
}
