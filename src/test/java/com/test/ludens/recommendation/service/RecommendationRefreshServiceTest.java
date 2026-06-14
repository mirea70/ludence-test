package com.test.ludens.recommendation.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.test.ludens.recommendation.domain.entity.RecommendationState;
import com.test.ludens.recommendation.repository.RecommendationStateRepository;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RecommendationRefreshService 테스트")
@ExtendWith(MockitoExtension.class)
class RecommendationRefreshServiceTest {

    @Mock
    private RecommendationStateRepository recommendationStateRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("추천 갱신을 요청하면 요청 버전을 원자적으로 증가시킨다")
    void incrementsRequestedVersion_whenRefreshIsRequested() {
        // given
        given(recommendationStateRepository.incrementRequestedVersion(1L)).willReturn(1L);
        RecommendationRefreshService service = service();

        // when
        service.requestRefresh(1L);

        // then
        verify(recommendationStateRepository).incrementRequestedVersion(1L);
        verify(userRepository, never()).findActiveByIdForUpdate(1L);
    }

    @Test
    @DisplayName("기존 사용자의 추천 갱신 상태가 없으면 상태를 초기화한다")
    void initializesState_whenRefreshStateDoesNotExist() {
        // given
        given(recommendationStateRepository.incrementRequestedVersion(1L))
                .willReturn(0L, 0L);
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.of(user()));

        // when
        service().requestRefresh(1L);

        // then
        verify(userRepository).findActiveByIdForUpdate(1L);
        verify(recommendationStateRepository).save(ArgumentMatchers.any(RecommendationState.class));
    }

    private RecommendationRefreshService service() {
        return new RecommendationRefreshService(recommendationStateRepository, userRepository);
    }

    private User user() {
        return User.create("viewer", "encoded-password", Instant.parse("2026-06-12T10:00:00Z"));
    }
}
