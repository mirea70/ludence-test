package com.test.ludens.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.recommendation.dto.response.RecommendationResponse;
import com.test.ludens.recommendation.repository.RecommendationQueryRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RecommendationQueryService 테스트")
@ExtendWith(MockitoExtension.class)
class RecommendationQueryServiceTest {

    @Mock
    private RecommendationQueryRepository recommendationQueryRepository;

    @Test
    @DisplayName("비로그인 사용자는 공통 추천을 조회한다")
    void getsCommonRecommendations_whenUserIsAnonymous() {
        // given
        given(recommendationQueryRepository.findCommon(null, 20)).willReturn(List.of(post(false)));
        given(recommendationQueryRepository.countActiveCommon()).willReturn(1L);

        // when
        RecommendationResponse result = service().getRecommendations(20, null);

        // then
        assertThat(result.total()).isEqualTo(1);
        verify(recommendationQueryRepository).findCommon(null, 20);
    }

    @Test
    @DisplayName("로그인 사용자는 개인 추천을 조회한다")
    void getsUserRecommendations_whenUserIsAuthenticated() {
        // given
        given(recommendationQueryRepository.findByUserId(7L, 10)).willReturn(List.of(post(true)));
        given(recommendationQueryRepository.countActiveByUserId(7L)).willReturn(1L);

        // when
        RecommendationResponse result = service().getRecommendations(10, 7L);

        // then
        assertThat(result.posts().getFirst().hearted()).isTrue();
        verify(recommendationQueryRepository).findByUserId(7L, 10);
    }

    private RecommendationQueryService service() {
        return new RecommendationQueryService(recommendationQueryRepository);
    }

    private PostDetailResponse post(boolean hearted) {
        Instant now = Instant.parse("2026-06-12T10:00:00Z");
        return new PostDetailResponse(1L, "title", null, now, now, "author", 1L, hearted);
    }
}
