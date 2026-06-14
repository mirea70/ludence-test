package com.test.ludens.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.post.dto.response.PostPageResponse;
import com.test.ludens.post.repository.PostRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@DisplayName("PostSearchService 테스트")
@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("검색어와 페이지 조건으로 게시글 검색 결과를 반환한다")
    void returnsPostPage_whenSearchRequestIsValid() {
        // given
        PageRequest pageRequest = new PageRequest(2, 10);
        PostDetailResponse detail = detail();
        given(postRepository.findActiveDetailsByQuery("spring", 7L, pageRequest)).willReturn(List.of(detail));
        given(postRepository.countActiveByQuery("spring")).willReturn(11L);
        PostSearchService service = new PostSearchService(postRepository, eventPublisher);

        // when
        PostPageResponse response = service.searchPosts("spring", 2, 10, 7L);

        // then
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.total()).isEqualTo(11);
        assertThat(response.posts()).containsExactly(detail);
        verify(eventPublisher).publishEvent(new com.test.ludens.search.domain.event.PostSearchedEvent(7L, "spring"));
    }

    @Test
    @DisplayName("검색어가 공백이면 전체 게시글 검색 조건으로 정규화한다")
    void normalizesQueryToNull_whenQueryIsBlank() {
        // given
        PageRequest pageRequest = new PageRequest(1, 20);
        given(postRepository.findActiveDetailsByQuery(null, null, pageRequest)).willReturn(List.of());
        given(postRepository.countActiveByQuery(null)).willReturn(0L);
        PostSearchService service = new PostSearchService(postRepository, eventPublisher);

        // when
        service.searchPosts("   ", 1, 20, null);

        // then
        verify(postRepository).findActiveDetailsByQuery(null, null, pageRequest);
        verify(postRepository).countActiveByQuery(null);
    }

    @Test
    @DisplayName("검색어가 100자를 초과하면 DomainException이 발생한다")
    void throwsDomainException_whenQueryExceedsMaximumLength() {
        // given
        PostSearchService service = new PostSearchService(postRepository, eventPublisher);

        // when & then
        assertThatThrownBy(() -> service.searchPosts("a".repeat(101), 1, 20, null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("공백 검색어도 100자를 초과하면 DomainException이 발생한다")
    void throwsDomainException_whenBlankQueryExceedsMaximumLength() {
        // given
        PostSearchService service = new PostSearchService(postRepository, eventPublisher);

        // when & then
        assertThatThrownBy(() -> service.searchPosts(" ".repeat(101), 1, 20, null))
                .isInstanceOf(DomainException.class);
    }

    private PostDetailResponse detail() {
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        return new PostDetailResponse(1L, "spring", null, createdAt, createdAt, "author", 1L, true);
    }
}
