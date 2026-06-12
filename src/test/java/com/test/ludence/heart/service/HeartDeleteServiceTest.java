package com.test.ludence.heart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.recommendation.service.RecommendationRefreshService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

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
    @DisplayName("활성 포스트의 하트를 삭제하면 집계 행을 잠그고 집계값을 감소시킨다")
    void deletesHeartAndDecreasesCount_whenPostAndHeartExist() {
        // given
        PostHeartCount heartCount = PostHeartCount.create(10L);
        heartCount.increment();
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.of(heartCount));
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.deleteByUserIdAndPostId(1L, 10L)).willReturn(1L);
        HeartDeleteService service = service();

        // when
        service.deleteHeart(1L, 10L);

        // then
        assertThat(heartCount.getCount()).isZero();
        verify(recommendationRefreshService).requestRefresh(1L);
    }

    @Test
    @DisplayName("하트 집계 행이 없으면 BusinessException이 발생한다")
    void throwsBusinessException_whenHeartCountDoesNotExist() {
        // given
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.empty());
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(postRepository, heartRepository);
    }

    @Test
    @DisplayName("포스트가 삭제되었으면 BusinessException이 발생한다")
    void throwsBusinessException_whenPostIsDeleted() {
        // given
        given(postHeartCountRepository.findByIdForUpdate(10L))
                .willReturn(Optional.of(PostHeartCount.create(10L)));
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(false);
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }

    @Test
    @DisplayName("삭제할 하트가 없으면 BusinessException이 발생하고 집계값을 감소시키지 않는다")
    void throwsBusinessExceptionAndPreservesCount_whenHeartDoesNotExist() {
        // given
        PostHeartCount heartCount = PostHeartCount.create(10L);
        heartCount.increment();
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.of(heartCount));
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.deleteByUserIdAndPostId(1L, 10L)).willReturn(0L);
        HeartDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deleteHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        assertThat(heartCount.getCount()).isEqualTo(1);
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
