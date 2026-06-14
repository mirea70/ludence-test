package com.test.ludens.recommendation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.recommendation.domain.entity.CommonRecommendation;
import com.test.ludens.recommendation.repository.CommonRecommendationRepository;
import com.test.ludens.support.IntegrationTestSupport;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("추천 API 통합 테스트")
class RecommendationIntegrationTest extends IntegrationTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostHeartCountRepository postHeartCountRepository;

    @Autowired
    private CommonRecommendationRepository commonRecommendationRepository;

    @Test
    @DisplayName("비로그인 사용자는 삭제 포스트를 제외한 공통 추천을 순위대로 조회한다")
    void returnsRankedActiveCommonRecommendations_whenUserIsAnonymous() throws Exception {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", NOW));
        Post first = savePost(author.getId(), "first");
        Post deleted = savePost(author.getId(), "deleted");
        Post second = savePost(author.getId(), "second");
        deleted.delete(NOW.plusSeconds(60));
        commonRecommendationRepository.saveAll(List.of(
                CommonRecommendation.create(first.getId(), 1, NOW),
                CommonRecommendation.create(deleted.getId(), 2, NOW),
                CommonRecommendation.create(second.getId(), 3, NOW)
        ));

        // when & then
        mockMvc.perform(get("/recommendation").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(first.getId()))
                .andExpect(jsonPath("$.posts[1].id").value(second.getId()))
                .andExpect(jsonPath("$.posts[0].hearted").value(false));
    }

    @Test
    @DisplayName("추천 limit이 유효하지 않으면 400을 반환한다")
    void returnsBadRequest_whenLimitIsInvalid() throws Exception {
        // when & then
        mockMvc.perform(get("/recommendation").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    private Post savePost(Long authorId, String title) {
        Post post = postRepository.save(Post.create(authorId, title, null, UUID.randomUUID() + ".png", NOW));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        return post;
    }
}
