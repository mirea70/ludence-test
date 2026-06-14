package com.test.ludens.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.recommendation.domain.entity.RecommendationState;
import com.test.ludens.support.JpaTestSupport;
import com.test.ludens.user.domain.entity.UserPostView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("신규 포스트 영향 사용자 조회 테스트")
class RecommendationAffectedUserRepositoryTest extends JpaTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");
    private static final Instant LAST_CALCULATED_AT = NOW.minusSeconds(3600);
    private static final Instant ACTIVITY_SINCE = NOW.minusSeconds(604800);

    @Test
    @DisplayName("하트한 포스트의 작성자가 마지막 추천 계산 이후 새 포스트를 작성한 사용자를 조회한다")
    void findsUsersAffectedByNewPostsFromHeartedAuthors() {
        // given
        RecommendationState affected = saveCalculatedState(1L);
        RecommendationState unaffected = saveCalculatedState(2L);
        Post affectedSource = savePost(10L, LAST_CALCULATED_AT.minusSeconds(10));
        Post unaffectedSource = savePost(20L, LAST_CALCULATED_AT.minusSeconds(10));
        heartRepository.save(Heart.create(affected.getUserId(), affectedSource.getId()));
        heartRepository.save(Heart.create(unaffected.getUserId(), unaffectedSource.getId()));
        savePost(10L, LAST_CALCULATED_AT.plusSeconds(10));
        savePost(20L, LAST_CALCULATED_AT.minusSeconds(1));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> result = recommendationStateRepository.findUserIdsAffectedByHeartedAuthors(100);

        // then
        assertThat(result).containsExactly(affected.getUserId());
    }

    @Test
    @DisplayName("최근 조회한 포스트의 작성자가 마지막 추천 계산 이후 새 포스트를 작성한 사용자를 조회한다")
    void findsUsersAffectedByNewPostsFromRecentlyViewedAuthors() {
        // given
        RecommendationState affected = saveCalculatedState(1L);
        RecommendationState oldViewer = saveCalculatedState(2L);
        Post source = savePost(10L, LAST_CALCULATED_AT.minusSeconds(10));
        userPostViewRepository.save(UserPostView.create(affected.getUserId(), source.getId(), ACTIVITY_SINCE.plusSeconds(1)));
        userPostViewRepository.save(UserPostView.create(oldViewer.getUserId(), source.getId(), ACTIVITY_SINCE.minusSeconds(1)));
        savePost(10L, LAST_CALCULATED_AT.plusSeconds(10));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> result = recommendationStateRepository.findUserIdsAffectedByViewedAuthors(ACTIVITY_SINCE, 100);

        // then
        assertThat(result).containsExactly(affected.getUserId());
    }

    private RecommendationState saveCalculatedState(Long userId) {
        RecommendationState state = RecommendationState.create(userId);
        state.completeCalculation(1L, LAST_CALCULATED_AT);
        return recommendationStateRepository.save(state);
    }

    private Post savePost(Long authorId, Instant createdAt) {
        return postRepository.save(Post.create(authorId, "title", null, UUID.randomUUID() + ".png", createdAt));
    }
}
