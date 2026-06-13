package com.test.ludence.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.post.domain.entity.PostViewCount;
import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.support.JpaTestSupport;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("추천 행동 기록 저장소 쿼리 테스트")
class RecommendationActivityRepositoryTest extends JpaTestSupport {

    private static final Instant RECORDED_AT = Instant.parse("2026-06-12T10:00:00Z");

    @Test
    @DisplayName("포스트 조회 수와 사용자 행동 이력을 수정 잠금으로 조회하고 추천 버전을 원자적으로 증가시킨다")
    void findsRecommendationActivitiesForUpdate() {
        // given
        postViewCountRepository.save(PostViewCount.create(10L));
        userPostViewRepository.save(UserPostView.create(1L, 10L, RECORDED_AT));
        userSearchKeywordRepository.save(UserSearchKeyword.create(1L, "spring", RECORDED_AT));
        recommendationStateRepository.save(RecommendationState.create(1L));
        entityManager.flush();
        entityManager.clear();

        // when
        long incrementedViewCount = postViewCountRepository.increment(10L);
        UserPostView postView = userPostViewRepository.findByUserIdAndPostIdForUpdate(1L, 10L).orElseThrow();
        UserSearchKeyword keyword = userSearchKeywordRepository
                .findByUserIdAndKeywordForUpdate(1L, "spring")
                .orElseThrow();
        long updatedCount = recommendationStateRepository.incrementRequestedVersion(1L);
        RecommendationState state = recommendationStateRepository.findById(1L).orElseThrow();

        // then
        assertThat(incrementedViewCount).isEqualTo(1L);
        assertThat(postViewCountRepository.findById(10L).orElseThrow().getCount()).isEqualTo(1L);
        assertThat(postView.getPostId()).isEqualTo(10L);
        assertThat(keyword.getKeyword()).isEqualTo("spring");
        assertThat(state.getUserId()).isEqualTo(1L);
        assertThat(updatedCount).isEqualTo(1L);
        assertThat(state.getRequestedVersion()).isEqualTo(2L);
    }

}
