package com.test.ludens.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.dto.request.PostUpdateRequest;
import com.test.ludens.post.dto.response.PostIdResponse;
import com.test.ludens.post.repository.PostRepository;
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

@DisplayName("PostUpdateService 테스트")
@ExtendWith(MockitoExtension.class)
class PostUpdateServiceTest {

    private static final Instant UPDATED_AT = Instant.parse("2026-06-10T10:00:00Z");
    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("작성자가 활성 포스트를 수정하면 포스트 ID를 반환한다")
    void returnsPostId_whenAuthorUpdatesActivePost() {
        // given
        Post post = post();
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.of(post));
        PostUpdateService service = new PostUpdateService(postRepository, fixedClock());

        // when
        PostIdResponse response = service.updatePost(1L, 10L, new PostUpdateRequest("after", "updated"));

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(post.getTitle()).isEqualTo("after");
        assertThat(post.getDescription()).isEqualTo("updated");
        assertThat(post.getEditedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("활성 포스트가 없으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActivePostDoesNotExist() {
        // given
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.empty());
        PostUpdateService service = new PostUpdateService(postRepository, fixedClock());

        // when & then
        assertThatThrownBy(() -> service.updatePost(1L, 10L, new PostUpdateRequest("after", null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("작성자가 아니면 DomainException이 발생한다")
    void throwsDomainException_whenUserIsNotAuthor() {
        // given
        Post post = post();
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.of(post));
        PostUpdateService service = new PostUpdateService(postRepository, fixedClock());

        // when & then
        assertThatThrownBy(() -> service.updatePost(2L, 10L, new PostUpdateRequest("after", null)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("제목과 설명이 null이면 기존 값과 수정 시각을 유지한다")
    void preservesValuesAndEditedAt_whenRequestHasNoChangeValue() {
        // given
        Post post = post();
        Instant previousEditedAt = post.getEditedAt();
        given(postRepository.findActiveByIdForUpdate(10L)).willReturn(Optional.of(post));
        PostUpdateService service = new PostUpdateService(postRepository, fixedClock());

        // when
        service.updatePost(1L, 10L, new PostUpdateRequest(null, null));

        // then
        assertThat(post.getTitle()).isEqualTo("before");
        assertThat(post.getDescription()).isNull();
        assertThat(post.getEditedAt()).isEqualTo(previousEditedAt);
    }

    private Post post() {
        Post post = Post.create(1L, "before", null, IMAGE_KEY, UPDATED_AT.minusSeconds(60));
        ReflectionTestUtils.setField(post, "id", 10L);
        return post;
    }

    private Clock fixedClock() {
        return Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
    }
}
