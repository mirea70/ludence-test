package com.test.ludens.recommendation.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("추천 후보 도메인 테스트")
class RecommendationCandidateTest {

    @Test
    @DisplayName("사용자 추천 후보와 공통 인기 후보를 유효한 순위로 생성한다")
    void createsCandidates_whenRankIsValid() {
        // given
        Instant calculatedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        UserRecommendation userRecommendation = UserRecommendation.create(1L, 2L, 1, calculatedAt);
        CommonRecommendation commonRecommendation = CommonRecommendation.create(2L, 40, calculatedAt);

        // then
        assertThat(userRecommendation.getRank()).isEqualTo(1);
        assertThat(commonRecommendation.getRank()).isEqualTo(40);
    }

    @Test
    @DisplayName("추천 후보의 계산 시각이 없으면 생성할 수 없다")
    void throwsDomainException_whenCalculatedAtIsNull() {
        // when & then
        assertThatThrownBy(() -> UserRecommendation.create(1L, 2L, 1, null))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> CommonRecommendation.create(2L, 1, null))
                .isInstanceOf(DomainException.class);
    }
}
