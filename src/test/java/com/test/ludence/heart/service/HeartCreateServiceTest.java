package com.test.ludence.heart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
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
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("HeartCreateService 테스트")
@ExtendWith(MockitoExtension.class)
class HeartCreateServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Mock
    private RecommendationRefreshService recommendationRefreshService;

    @Test
    @DisplayName("활성 포스트에 하트를 추가하면 집계값을 원자적으로 증가시키고 하트를 저장한다")
    void savesHeartAndIncreasesCount_whenPostIsActive() {
        // given
        given(postHeartCountRepository.increase(10L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        HeartCreateService service = service();

        // when
        service.createHeart(1L, 10L);

        // then
        InOrder inOrder = inOrder(postHeartCountRepository, postRepository, heartRepository);
        inOrder.verify(postHeartCountRepository).increase(10L);
        inOrder.verify(postRepository).existsByIdAndDeletedAtIsNull(10L);
        inOrder.verify(heartRepository).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(recommendationRefreshService).requestRefresh(1L);
    }

    @Test
    @DisplayName("하트 집계 행이 존재하지 않으면 BusinessException이 발생한다")
    void throwsHeartCountNotFound_whenHeartCountDoesNotExist() {
        // given
        given(postHeartCountRepository.increase(10L)).willReturn(0L);
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorInfo()).isEqualTo(HeartErrorInfo.NOT_FOUND_COUNT));
        verifyNoInteractions(postRepository, heartRepository);
    }

    @Test
    @DisplayName("포스트가 삭제되었으면 BusinessException이 발생한다")
    void throwsBusinessException_whenPostIsDeleted() {
        // given
        given(postHeartCountRepository.increase(10L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(false);
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }

    @Test
    @DisplayName("DB 복합키 제약으로 중복 하트 저장이 실패하면 BusinessException이 발생한다")
    void throwsBusinessException_whenDatabaseRejectsDuplicateHeart() {
        // given
        given(postHeartCountRepository.increase(10L)).willReturn(1L);
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .willThrow(DataIntegrityViolationException.class);
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verify(postHeartCountRepository).increase(10L);
    }

    private HeartCreateService service() {
        return new HeartCreateService(
                postRepository,
                heartRepository,
                postHeartCountRepository,
                recommendationRefreshService
        );
    }
}
