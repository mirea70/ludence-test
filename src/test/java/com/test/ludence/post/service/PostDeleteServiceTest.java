package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.repository.PostRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("PostDeleteService 테스트")
@ExtendWith(MockitoExtension.class)
class PostDeleteServiceTest {

    private static final Instant DELETED_AT = Instant.parse("2026-06-10T10:00:00Z");
    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Test
    @DisplayName("작성자가 포스트를 삭제하면 소프트 삭제하고 하트와 집계를 정리한다")
    void softDeletesPostAndClearsHearts_whenAuthorDeletesPost() {
        // given
        Post post = post();
        PostHeartCount heartCount = PostHeartCount.create(10L);
        heartCount.increment();
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.of(post));
        given(postHeartCountRepository.findById(10L)).willReturn(Optional.of(heartCount));
        PostDeleteService service = service();

        // when
        service.deletePost(1L, 10L);

        // then
        assertThat(post.getDeletedAt()).isEqualTo(DELETED_AT);
        assertThat(heartCount.getCount()).isZero();
        verify(heartRepository).deleteByPostId(10L);
    }

    @Test
    @DisplayName("활성 포스트가 없으면 BusinessException이 발생하고 하트를 변경하지 않는다")
    void throwsBusinessException_whenActivePostDoesNotExist() {
        // given
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.empty());
        PostDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deletePost(1L, 10L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository, postHeartCountRepository);
    }

    @Test
    @DisplayName("작성자가 아니면 DomainException이 발생하고 하트를 변경하지 않는다")
    void throwsDomainException_whenUserIsNotAuthor() {
        // given
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.of(post()));
        PostDeleteService service = service();

        // when & then
        assertThatThrownBy(() -> service.deletePost(2L, 10L))
                .isInstanceOf(DomainException.class);
        verifyNoInteractions(heartRepository, postHeartCountRepository);
    }

    private PostDeleteService service() {
        return new PostDeleteService(postRepository, heartRepository, postHeartCountRepository, fixedClock());
    }

    private Post post() {
        Post post = Post.create(1L, "title", null, IMAGE_KEY, DELETED_AT.minusSeconds(60));
        ReflectionTestUtils.setField(post, "id", 10L);
        return post;
    }

    private Clock fixedClock() {
        return Clock.fixed(DELETED_AT, ZoneOffset.UTC);
    }
}
