package com.test.ludence.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.recommendation.domain.entity.UserRecommendation;
import com.test.ludence.recommendation.repository.CommonRecommendationRepository;
import com.test.ludence.recommendation.repository.RecommendationCandidateQueryRepository;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.recommendation.repository.UserRecommendationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RecommendationCalculationService 테스트")
@ExtendWith(MockitoExtension.class)
class RecommendationCalculationServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");

    @Mock
    private RecommendationCandidateQueryRepository candidateQueryRepository;
    @Mock
    private CommonRecommendationRepository commonRecommendationRepository;
    @Mock
    private UserRecommendationRepository userRecommendationRepository;
    @Mock
    private RecommendationStateRepository recommendationStateRepository;

    @Test
    @DisplayName("사용자 추천은 우선순위별 후보의 중복을 제거하고 최대 40개까지 교체한다")
    void replacesUserRecommendationsWithDistinctCandidates() {
        // given
        RecommendationState state = RecommendationState.create(1L);
        given(recommendationStateRepository.findByUserIdForUpdate(1L)).willReturn(java.util.Optional.of(state));
        given(candidateQueryRepository.findByHeartedAuthors(1L, 40)).willReturn(List.of(2L, 3L));
        given(candidateQueryRepository.findByViewedAuthors(1L, NOW.minusSeconds(604800), 40)).willReturn(List.of(3L, 4L));
        given(candidateQueryRepository.findByRecentSearches(1L, NOW.minusSeconds(604800), 40)).willReturn(List.of(4L, 5L));
        given(candidateQueryRepository.findLatest(1L, 40)).willReturn(List.of(5L, 6L));
        RecommendationCalculationService service = service();

        // when
        service.calculateUserRecommendations(1L);

        // then
        ArgumentCaptor<List<UserRecommendation>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRecommendationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(UserRecommendation::getPostId)
                .containsExactly(2L, 3L, 4L, 5L, 6L);
        assertThat(captor.getValue()).extracting(UserRecommendation::getRank)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(state.getCalculatedVersion()).isEqualTo(state.getRequestedVersion());
    }

    private RecommendationCalculationService service() {
        return new RecommendationCalculationService(
                candidateQueryRepository,
                commonRecommendationRepository,
                userRecommendationRepository,
                recommendationStateRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }
}
