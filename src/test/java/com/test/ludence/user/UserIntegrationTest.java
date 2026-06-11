package com.test.ludence.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.support.IntegrationTestSupport;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("회원 API 통합 테스트")
class UserIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostHeartCountRepository postHeartCountRepository;

    @Autowired
    private HeartRepository heartRepository;

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

    @Test
    @DisplayName("회원 게시글을 조회하면 최신순 페이지와 현재 회원의 하트 여부를 반환한다")
    void returnsLatestPostPageWithHearted_whenUserPostsAreQueried() throws Exception {
        // given
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        User author = userRepository.save(User.create("author", "encoded-password", createdAt));
        savePost(author.getId(), "old", "550e8400-e29b-41d4-a716-446655440001.png", createdAt);
        Post newPost = savePost(
                author.getId(), "new", "550e8400-e29b-41d4-a716-446655440002.png", createdAt.plusSeconds(60)
        );
        String viewerToken = signupAndGetToken("viewer");
        User viewer = userRepository.findByUsernameValueAndDeletedAtIsNull("viewer").orElseThrow();
        PostHeartCount newPostHeartCount = postHeartCountRepository.findById(newPost.getId()).orElseThrow();
        newPostHeartCount.increment();
        heartRepository.save(Heart.create(viewer.getId(), newPost.getId()));

        // when & then
        mockMvc.perform(get("/users/author/posts")
                        .param("page", "1")
                        .param("limit", "1")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(newPost.getId()))
                .andExpect(jsonPath("$.posts[0].title").value("new"))
                .andExpect(jsonPath("$.posts[0].heartCount").value(1))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));
    }

    @Test
    @DisplayName("회원 게시글 조회의 페이지 조건이 유효하지 않으면 400을 반환한다")
    void returnsBadRequest_whenUserPostPageIsInvalid() throws Exception {
        // given
        userRepository.save(User.create("author", "encoded-password", Instant.parse("2026-06-10T10:00:00Z")));

        // when & then
        mockMvc.perform(get("/users/author/posts").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    @Test
    @DisplayName("본인이 하트한 활성 포스트를 조회하면 최신순 페이지 응답을 반환한다")
    void returnsLatestHeartedPosts_whenRequesterIsUser() throws Exception {
        // given
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        User author = userRepository.save(User.create("author", "encoded-password", createdAt));
        Post oldPost = savePost(author.getId(), "old", "550e8400-e29b-41d4-a716-446655440030.png", createdAt);
        Post newPost = savePost(author.getId(), "new", "550e8400-e29b-41d4-a716-446655440031.png",
                createdAt.plusSeconds(60));
        String viewerToken = signupAndGetToken("viewer");
        User viewer = userRepository.findByUsernameValueAndDeletedAtIsNull("viewer").orElseThrow();
        heartRepository.save(Heart.create(viewer.getId(), oldPost.getId()));
        heartRepository.save(Heart.create(viewer.getId(), newPost.getId()));

        // when & then
        mockMvc.perform(get("/users/viewer/hearts")
                        .param("page", "1")
                        .param("limit", "1")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(newPost.getId()))
                .andExpect(jsonPath("$.posts[0].hearted").value(true));
    }

    @Test
    @DisplayName("다른 회원의 하트 목록 조회는 403이고 없는 회원 조회는 404이다")
    void rejectsUserHeartQuery_whenRequesterIsNotUserOrUserDoesNotExist() throws Exception {
        // given
        String viewerToken = signupAndGetToken("viewer");
        signupAndGetToken("other");

        // when & then
        mockMvc.perform(get("/users/other/hearts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_005"));

        mockMvc.perform(get("/users/unknown/hearts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_004"));
    }

    @Test
    @DisplayName("인증 없이 하트 포스트 목록을 조회하면 401을 반환한다")
    void returnsUnauthorized_whenUserHeartQueryIsAnonymous() throws Exception {
        // when & then
        mockMvc.perform(get("/users/sunny/hearts"))
                .andExpect(status().isUnauthorized());
    }

    private Post savePost(Long authorId, String title, String imageKey, Instant createdAt) {
        Post post = postRepository.save(Post.create(authorId, title, null, imageKey, createdAt));
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
