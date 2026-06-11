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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("활성 포스트에 하트를 추가하면 집계 행을 잠그고 INSERT 성공 후 집계값을 증가시킨다")
    void savesHeartAndIncreasesCount_whenPostIsActive() {
        // given
        PostHeartCount heartCount = PostHeartCount.create(10L);
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.of(heartCount));
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        HeartCreateService service = service();

        // when
        service.createHeart(1L, 10L);

        // then
        assertThat(heartCount.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("하트 집계 행이 존재하지 않으면 BusinessException이 발생한다")
    void throwsBusinessException_whenHeartCountDoesNotExist() {
        // given
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.empty());
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
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
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }

    @Test
    @DisplayName("DB 복합키 제약으로 중복 하트 저장이 실패하면 집계값을 증가시키지 않는다")
    void doesNotIncreaseCount_whenDatabaseRejectsDuplicateHeart() {
        // given
        PostHeartCount heartCount = PostHeartCount.create(10L);
        given(postHeartCountRepository.findByIdForUpdate(10L)).willReturn(Optional.of(heartCount));
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(heartRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .willThrow(DataIntegrityViolationException.class);
        HeartCreateService service = service();

        // when & then
        assertThatThrownBy(() -> service.createHeart(1L, 10L))
                .isInstanceOf(BusinessException.class);
        assertThat(heartCount.getCount()).isZero();
    }

    private HeartCreateService service() {
        return new HeartCreateService(postRepository, heartRepository, postHeartCountRepository);
    }
}
