package com.test.ludens.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.post.dto.response.PostPageResponse;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserHeartQueryService 테스트")
@ExtendWith(MockitoExtension.class)
class UserHeartQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("본인이 하트한 포스트를 조회하면 페이지 응답을 반환한다")
    void returnsHeartedPostPage_whenRequesterIsUser() {
        // given
        PageRequest pageRequest = new PageRequest(2, 10);
        PostDetailResponse detail = detail();
        given(userRepository.findActiveIdByUsername("sunny")).willReturn(Optional.of(1L));
        given(postRepository.findActiveDetailsHeartedByUserId(1L, pageRequest)).willReturn(List.of(detail));
        given(postRepository.countActiveHeartedByUserId(1L)).willReturn(11L);
        UserHeartQueryService service = new UserHeartQueryService(userRepository, postRepository);

        // when
        PostPageResponse response = service.getUserHearts(1L, "sunny", 2, 10);

        // then
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(11);
        assertThat(response.posts()).containsExactly(detail);
    }

    @Test
    @DisplayName("활성 회원이 없으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActiveUserDoesNotExist() {
        // given
        given(userRepository.findActiveIdByUsername("sunny")).willReturn(Optional.empty());
        UserHeartQueryService service = new UserHeartQueryService(userRepository, postRepository);

        // when & then
        assertThatThrownBy(() -> service.getUserHearts(1L, "sunny", 1, 20))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("요청자가 해당 회원이 아니면 BusinessException이 발생한다")
    void throwsBusinessException_whenRequesterIsNotUser() {
        // given
        given(userRepository.findActiveIdByUsername("sunny")).willReturn(Optional.of(2L));
        UserHeartQueryService service = new UserHeartQueryService(userRepository, postRepository);

        // when & then
        assertThatThrownBy(() -> service.getUserHearts(1L, "sunny", 1, 20))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(postRepository);
    }

    private PostDetailResponse detail() {
        Instant createdAt = Instant.parse("2026-06-10T10:00:00Z");
        return new PostDetailResponse(1L, "title", null, createdAt, createdAt, "author", 1L, true);
    }
}
