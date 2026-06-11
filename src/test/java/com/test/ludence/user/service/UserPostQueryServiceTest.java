package com.test.ludence.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostPageResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserPostQueryService 테스트")
@ExtendWith(MockitoExtension.class)
class UserPostQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("활성 회원의 게시글을 조회하면 페이지 응답을 반환한다")
    void returnsPostPage_whenActiveUserExists() {
        // given
        PageRequest pageRequest = new PageRequest(2, 10);
        PostDetailResponse detail = detail();
        given(userRepository.findActiveIdByUsername("sunny")).willReturn(Optional.of(1L));
        given(postRepository.findActiveDetailsByAuthorId(1L, "sunny", 7L, pageRequest)).willReturn(List.of(detail));
        given(postRepository.countActiveByAuthorId(1L)).willReturn(11L);
        UserPostQueryService service = new UserPostQueryService(userRepository, postRepository);

        // when
        PostPageResponse response = service.getUserPosts("sunny", 2, 10, 7L);

        // then
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(11);
        assertThat(response.posts()).containsExactly(detail);
    }

    @Test
    @DisplayName("활성 회원이 존재하지 않으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActiveUserDoesNotExist() {
        // given
        given(userRepository.findActiveIdByUsername("sunny")).willReturn(Optional.empty());
        UserPostQueryService service = new UserPostQueryService(userRepository, postRepository);

        // when & then
        assertThatThrownBy(() -> service.getUserPosts("sunny", 1, 20, null))
                .isInstanceOf(BusinessException.class);
    }

    private PostDetailResponse detail() {
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        return new PostDetailResponse(1L, "title", null, createdAt, createdAt, "sunny", 1L, true);
    }
}
