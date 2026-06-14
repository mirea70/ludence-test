package com.test.ludens.recommendation.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecommendationRank 도메인 테스트")
class RecommendationRankTest {

    @Test
    @DisplayName("추천 후보 순위는 1부터 40까지 생성할 수 있다")
    void createsRank_whenRankIsWithinCandidateLimit() {
        // when & then
        assertThat(new RecommendationRank(1).value()).isEqualTo(1);
        assertThat(new RecommendationRank(40).value()).isEqualTo(40);
    }

    @Test
    @DisplayName("추천 후보 순위가 1부터 40 사이가 아니면 DomainException이 발생한다")
    void throwsDomainException_whenRankIsOutsideCandidateLimit() {
        // when & then
        assertThatThrownBy(() -> new RecommendationRank(0))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new RecommendationRank(41))
                .isInstanceOf(DomainException.class);
    }
}
