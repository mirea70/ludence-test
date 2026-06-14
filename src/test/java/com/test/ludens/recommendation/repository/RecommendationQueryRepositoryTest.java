package com.test.ludens.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.recommendation.domain.entity.CommonRecommendation;
import com.test.ludens.recommendation.domain.entity.UserRecommendation;
import com.test.ludens.support.JpaTestSupport;
import com.test.ludens.user.domain.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecommendationQueryRepository 테스트")
class RecommendationQueryRepositoryTest extends JpaTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");

    @Test
    @DisplayName("공통 추천은 삭제 포스트를 제외하고 저장된 순위대로 조회한다")
    void findsCommonRecommendationsInRankOrderExcludingDeletedPosts() {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", NOW));
        Post second = savePost(author.getId(), "second");
        Post deleted = savePost(author.getId(), "deleted");
        Post first = savePost(author.getId(), "first");
        deleted.delete(NOW.plusSeconds(60));
        commonRecommendationRepository.saveAll(List.of(
                CommonRecommendation.create(first.getId(), 1, NOW),
                CommonRecommendation.create(deleted.getId(), 2, NOW),
                CommonRecommendation.create(second.getId(), 3, NOW)
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PostDetailResponse> result = recommendationQueryRepository.findCommon(null, 2);

        // then
        assertThat(result).extracting(PostDetailResponse::title).containsExactly("first", "second");
        assertThat(result).allMatch(post -> !post.hearted());
        assertThat(recommendationQueryRepository.countActiveCommon()).isEqualTo(2);
    }

    @Test
    @DisplayName("개인 추천은 저장된 순위와 현재 사용자의 하트 여부를 함께 조회한다")
    void findsUserRecommendationsWithHeartedInRankOrder() {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", NOW));
        User viewer = userRepository.save(User.create("viewer", "encoded-password", NOW));
        Post first = savePost(author.getId(), "first");
        Post second = savePost(author.getId(), "second");
        heartRepository.save(Heart.create(viewer.getId(), first.getId()));
        userRecommendationRepository.saveAll(List.of(
                UserRecommendation.create(viewer.getId(), first.getId(), 1, NOW),
                UserRecommendation.create(viewer.getId(), second.getId(), 2, NOW)
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PostDetailResponse> result = recommendationQueryRepository.findByUserId(viewer.getId(), 20);

        // then
        assertThat(result).extracting(PostDetailResponse::title).containsExactly("first", "second");
        assertThat(result.getFirst().hearted()).isTrue();
        assertThat(recommendationQueryRepository.countActiveByUserId(viewer.getId())).isEqualTo(2);
    }

    private Post savePost(Long authorId, String title) {
        Post post = postRepository.save(Post.create(authorId, title, null, UUID.randomUUID() + ".png", NOW));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        return post;
    }
}
