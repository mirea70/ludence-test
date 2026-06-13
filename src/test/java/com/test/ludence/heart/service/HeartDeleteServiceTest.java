package com.test.ludence.heart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.info.HeartErrorInfo;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.recommendation.service.RecommendationRefreshService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("HeartDeleteService 테스트")
@ExtendWith(MockitoExtension.class)
class HeartDeleteServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Mock
    private RecommendationRefreshService recommendationRefreshService;

    @Test
    @DisplayName("활성 포스트의 하트를 삭제하면 집계값을 원자적으로 감소시키고 하트를 삭제한다")
    void deletesHeartAndDecreasesCount_whenPostAndHeartExist() {
        // given
        given(postHeartCountRepository.decrease(10L, 1L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.deleteByUserIdAndPostId(1L, 10L)).willReturn(1L);
        HeartDeleteService service = service();

        // when
        service.deleteHeart(1L, 10L);

        // then
        InOrder inOrder = inOrder(postHeartCountRepository, postRepository, heartRepository);
        inOrder.verify(postHeartCountRepository).decrease(10L, 1L);
        inOrder.verify(postRepository).existsByIdAndDeletedAtIsNull(10L);
        inOrder.verify(heartRepository).deleteByUserIdAndPostId(1L, 10L);
        verify(recommendationRefreshService).requestRefresh(1L);
    }

    @Test
    @DisplayName("활성 포스트의 하트 집계값이 0이면 하트 없음 예외가 발생한다")
    void throwsHeartCountNotFound_whenHeartCountIsZero() {
        // given
        given(postHeartCountRepository.decrease(10L, 1L)).willReturn(0L);
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorInfo()).isEqualTo(HeartErrorInfo.NOT_FOUND_COUNT));
        verifyNoInteractions(postRepository, heartRepository);
    }

    @Test
    @DisplayName("원자적 감소 후 포스트가 삭제 상태이면 포스트 없음 예외가 발생한다")
    void throwsBusinessException_whenPostIsDeleted() {
        // given
        given(postHeartCountRepository.decrease(10L, 1L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(false);
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }

    @Test
    @DisplayName("삭제할 하트 관계가 없으면 하트 없음 예외가 발생한다")
    void throwsBusinessException_whenHeartDoesNotExist() {
        // given
        given(postHeartCountRepository.decrease(10L, 1L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.deleteByUserIdAndPostId(1L, 10L)).willReturn(0L);
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verify(postHeartCountRepository).decrease(10L, 1L);
    }

    private HeartDeleteService service() {
        return new HeartDeleteService(
                postRepository,
                heartRepository,
                postHeartCountRepository,
                recommendationRefreshService
        );
    }
}
