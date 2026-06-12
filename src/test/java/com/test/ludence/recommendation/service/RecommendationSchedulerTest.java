package com.test.ludence.recommendation.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RecommendationScheduler 테스트")
@ExtendWith(MockitoExtension.class)
class RecommendationSchedulerTest {

    @Mock
    private RecommendationCalculationService calculationService;
    @Mock
    private RecommendationStateRepository recommendationStateRepository;

    @Test
    @DisplayName("주기 실행 시 공통 추천과 갱신 대기 사용자의 추천을 계산한다")
    void calculatesCommonAndPendingUserRecommendations() {
        // given
        given(recommendationStateRepository.findPendingUserIds(100)).willReturn(List.of(1L, 2L));
        RecommendationScheduler scheduler = new RecommendationScheduler(calculationService, recommendationStateRepository);

        // when
        scheduler.calculateRecommendations();

        // then
        verify(calculationService).calculateCommonRecommendations();
        verify(calculationService).calculateUserRecommendations(1L);
        verify(calculationService).calculateUserRecommendations(2L);
    }

    @Test
    @DisplayName("한 사용자의 추천 계산이 실패해도 다음 사용자의 추천을 계산한다")
    void continuesCalculating_whenUserCalculationFails() {
        // given
        given(recommendationStateRepository.findPendingUserIds(100)).willReturn(List.of(1L, 2L));
        doThrow(new IllegalStateException()).when(calculationService).calculateUserRecommendations(1L);
        RecommendationScheduler scheduler = new RecommendationScheduler(calculationService, recommendationStateRepository);

        // when
        scheduler.calculateRecommendations();

        // then
        verify(calculationService).calculateUserRecommendations(2L);
    }
}
