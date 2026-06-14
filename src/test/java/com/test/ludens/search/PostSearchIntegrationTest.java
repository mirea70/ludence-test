package com.test.ludens.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.ludens.auth.dto.request.AuthRequest;
import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.heart.repository.HeartRepository;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.support.IntegrationTestSupport;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("게시글 검색 API 통합 테스트")
class PostSearchIntegrationTest extends IntegrationTestSupport {

    private static final Instant CREATED_AT = Instant.parse("2026-06-10T10:00:00Z");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostHeartCountRepository postHeartCountRepository;

    @Autowired
    private HeartRepository heartRepository;

    @Test
    @DisplayName("검색어와 인증 사용자로 검색하면 일치하는 활성 게시글과 하트 정보를 최신순으로 반환한다")
    void returnsMatchedActivePostsWithHeartedInLatestOrder_whenSearchIsAuthenticated() throws Exception {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        Post oldTitleMatch = savePost(author.getId(), "Spring guide", null, 1, 60);
        Post newDescriptionMatch = savePost(author.getId(), "JPA", "SPRING data", 2, 120);
        savePost(author.getId(), "Java", "QueryDSL", 3, 180);
        Post deletedMatch = savePost(author.getId(), "spring deleted", null, 4, 240);
        deletedMatch.delete(CREATED_AT.plusSeconds(300));

        String viewerToken = signupAndGetToken("viewer");
        User viewer = userRepository.findByUsernameValueAndDeletedAtIsNull("viewer").orElseThrow();
        PostHeartCount heartCount = postHeartCountRepository.findById(newDescriptionMatch.getId()).orElseThrow();
        heartCount.increment();
        heartRepository.save(Heart.create(viewer.getId(), newDescriptionMatch.getId()));

        // when & then
        mockMvc.perform(get("/search/posts")
                        .param("q", "spring")
                        .param("page", "1")
                        .param("limit", "1")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(newDescriptionMatch.getId()))
                .andExpect(jsonPath("$.posts[0].title").value("JPA"))
                .andExpect(jsonPath("$.posts[0].heartCount").value(1))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));

        mockMvc.perform(get("/search/posts")
                        .param("q", "spring")
                        .param("page", "2")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(oldTitleMatch.getId()))
                .andExpect(jsonPath("$.posts[0].hearted").value(false));
    }

    @Test
    @DisplayName("검색어가 없으면 활성 게시글 전체를 기본 페이지 조건으로 반환한다")
    void returnsAllActivePostsWithDefaults_whenQueryIsOmitted() throws Exception {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        Post oldPost = savePost(author.getId(), "old", null, 10, 60);
        Post newPost = savePost(author.getId(), "new", null, 11, 120);
        Post deletedPost = savePost(author.getId(), "deleted", null, 12, 180);
        deletedPost.delete(CREATED_AT.plusSeconds(240));

        // when & then
        mockMvc.perform(get("/search/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(newPost.getId()))
                .andExpect(jsonPath("$.posts[1].id").value(oldPost.getId()));
    }

    @Test
    @DisplayName("검색어나 페이지 조건이 유효하지 않으면 400을 반환한다")
    void returnsBadRequest_whenSearchRequestIsInvalid() throws Exception {
        // when & then
        mockMvc.perform(get("/search/posts").param("q", "a".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SEARCH_001"));

        mockMvc.perform(get("/search/posts").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));

        mockMvc.perform(get("/search/posts").param("page", "not-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_003"));
    }

    private Post savePost(Long authorId, String title, String description, int imageSuffix, long createdOffset) {
        Post post = postRepository.save(Post.create(
                authorId,
                title,
                description,
                new UUID(0L, imageSuffix).toString() + ".png",
                CREATED_AT.plusSeconds(createdOffset)
        ));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        return post;
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
}
