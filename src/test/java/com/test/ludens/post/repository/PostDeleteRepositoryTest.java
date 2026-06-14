package com.test.ludens.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.domain.entity.PostViewCount;
import com.test.ludens.recommendation.domain.entity.CommonRecommendation;
import com.test.ludens.recommendation.domain.entity.UserRecommendation;
import com.test.ludens.support.JpaTestSupport;
import com.test.ludens.user.domain.entity.UserPostView;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("포스트 삭제 저장소 쿼리 테스트")
class PostDeleteRepositoryTest extends JpaTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");

    @Test
    @DisplayName("포스트 ID에 해당하는 하트를 모두 삭제한다")
    void deletesHeartsByPostId() {
        // given
        heartRepository.save(Heart.create(1L, 10L));
        heartRepository.save(Heart.create(2L, 10L));
        heartRepository.save(Heart.create(1L, 20L));
        entityManager.flush();
        entityManager.clear();

        // when
        long deletedCount = heartRepository.deleteByPostId(10L);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(deletedCount).isEqualTo(2);
        assertThat(heartRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("30일 경과한 삭제 포스트와 연관 데이터를 물리 삭제한다")
    void deletesExpiredPostAndRelatedData() {
        // given
        Post post = postRepository.save(Post.create(
                1L,
                "title",
                null,
                "550e8400-e29b-41d4-a716-446655440000.png",
                NOW.minusSeconds(31L * 24 * 60 * 60)
        ));
        post.delete(NOW.minusSeconds(31L * 24 * 60 * 60));
        entityManager.flush();
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        postViewCountRepository.save(PostViewCount.create(post.getId()));
        heartRepository.save(Heart.create(1L, post.getId()));
        userPostViewRepository.save(UserPostView.create(1L, post.getId(), NOW));
        commonRecommendationRepository.save(CommonRecommendation.create(post.getId(), 1, NOW));
        userRecommendationRepository.save(UserRecommendation.create(1L, post.getId(), 1, NOW));
        entityManager.flush();
        entityManager.clear();

        // when
        long deletedCount = postRepository.deleteExpiredPostData(post.getId(), NOW.minusSeconds(30L * 24 * 60 * 60));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(postRepository.findById(post.getId())).isEmpty();
        assertThat(postHeartCountRepository.findById(post.getId())).isEmpty();
        assertThat(postViewCountRepository.findById(post.getId())).isEmpty();
        assertThat(heartRepository.count()).isZero();
        assertThat(userPostViewRepository.count()).isZero();
        assertThat(commonRecommendationRepository.count()).isZero();
        assertThat(userRecommendationRepository.count()).isZero();
    }
}
