package com.test.ludence.recommendation.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecommendationState 도메인 테스트")
class RecommendationStateTest {

    private static final Instant CALCULATED_AT = Instant.parse("2026-06-12T10:00:00Z");

    @Test
    @DisplayName("추천 상태 생성 직후에는 한 번의 추천 계산이 필요하다")
    void requiresCalculation_whenStateIsCreated() {
        // when
        RecommendationState state = RecommendationState.create(1L);

        // then
        assertThat(state.getRequestedVersion()).isEqualTo(1L);
        assertThat(state.getCalculatedVersion()).isZero();
        assertThat(state.getLastCalculatedAt()).isNull();
        assertThat(state.requiresCalculation()).isTrue();
    }

    @Test
    @DisplayName("추천 계산 완료 후 새 갱신 요청이 없으면 계산이 필요하지 않다")
    void doesNotRequireCalculation_whenRequestedVersionIsCompleted() {
        // given
        RecommendationState state = RecommendationState.create(1L);

        // when
        state.completeCalculation(1L, CALCULATED_AT);

        // then
        assertThat(state.requiresCalculation()).isFalse();
        assertThat(state.getLastCalculatedAt()).isEqualTo(CALCULATED_AT);
    }

    @Test
    @DisplayName("추천 계산 중 새 갱신 요청이 발생하면 계산 완료 후에도 다시 계산해야 한다")
    void requiresCalculation_whenRefreshIsRequestedDuringCalculation() {
        // given
        RecommendationState state = RecommendationState.create(1L);
        long calculatingVersion = state.getRequestedVersion();
        state.requestRefresh();

        // when
        state.completeCalculation(calculatingVersion, CALCULATED_AT);

        // then
        assertThat(state.requiresCalculation()).isTrue();
    }

    @Test
    @DisplayName("요청되지 않은 미래 버전으로 계산 완료할 수 없다")
    void throwsDomainException_whenFutureVersionIsCompleted() {
        // given
        RecommendationState state = RecommendationState.create(1L);

        // when & then
        assertThatThrownBy(() -> state.completeCalculation(2L, CALCULATED_AT))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("이전 계산 시각보다 과거 시각으로 계산 완료할 수 없다")
    void throwsDomainException_whenCalculationTimeMovesBackward() {
        // given
        RecommendationState state = RecommendationState.create(1L);
        state.completeCalculation(1L, CALCULATED_AT);
        state.requestRefresh();

        // when & then
        assertThatThrownBy(() -> state.completeCalculation(2L, CALCULATED_AT.minusSeconds(1)))
                .isInstanceOf(DomainException.class);
    }
}
