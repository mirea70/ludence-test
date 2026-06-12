package com.test.ludence.recommendation.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RecommendationRefreshService 테스트")
@ExtendWith(MockitoExtension.class)
class RecommendationRefreshServiceTest {

    @Mock
    private RecommendationStateRepository recommendationStateRepository;

    @Test
    @DisplayName("추천 갱신을 요청하면 요청 버전을 원자적으로 증가시킨다")
    void incrementsRequestedVersion_whenRefreshIsRequested() {
        // given
        given(recommendationStateRepository.incrementRequestedVersion(1L)).willReturn(1L);
        RecommendationRefreshService service = new RecommendationRefreshService(recommendationStateRepository);

        // when
        service.requestRefresh(1L);

        // then
        verify(recommendationStateRepository).incrementRequestedVersion(1L);
    }
}
