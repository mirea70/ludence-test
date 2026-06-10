package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostResponse;
import com.test.ludence.post.repository.PostRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostQueryService 테스트")
@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("활성 포스트가 존재하면 상세 응답을 반환한다")
    void returnsPostResponse_whenActivePostExists() {
        // given
        PostDetailResponse detail = detail();
        given(postRepository.findActiveDetailById(1L, 2L)).willReturn(Optional.of(detail));
        PostQueryService service = new PostQueryService(postRepository);

        // when
        PostResponse response = service.getPost(1L, 2L);

        // then
        assertThat(response.post()).isEqualTo(detail);
    }

    @Test
    @DisplayName("활성 포스트가 없으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActivePostDoesNotExist() {
        // given
        given(postRepository.findActiveDetailById(1L, null)).willReturn(Optional.empty());
        PostQueryService service = new PostQueryService(postRepository);

        // when & then
        assertThatThrownBy(() -> service.getPost(1L, null))
                .isInstanceOf(BusinessException.class);
    }

    private PostDetailResponse detail() {
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        return new PostDetailResponse(1L, "title", "description", createdAt, createdAt, "author", 1L, true);
    }
}
