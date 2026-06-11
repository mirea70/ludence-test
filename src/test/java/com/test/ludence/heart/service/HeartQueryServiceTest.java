package com.test.ludence.heart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.heart.dto.response.HeartUserPageResponse;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.post.repository.PostHeartAccess;
import com.test.ludence.post.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("HeartQueryService 테스트")
@ExtendWith(MockitoExtension.class)
class HeartQueryServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @Test
    @DisplayName("포스트 작성자가 하트 회원 목록을 조회하면 페이지 응답을 반환한다")
    void returnsHeartUserPage_whenRequesterIsAuthor() {
        // given
        PageRequest pageRequest = new PageRequest(2, 10);
        given(postRepository.findActiveHeartAccessById(20L)).willReturn(Optional.of(new PostHeartAccess(1L, 11L)));
        given(heartRepository.findActiveUsernamesByPostId(20L, pageRequest)).willReturn(List.of("new", "old"));
        HeartQueryService service = new HeartQueryService(postRepository, heartRepository);

        // when
        HeartUserPageResponse response = service.getPostHearts(1L, 20L, 2, 10);

        // then
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.total()).isEqualTo(11);
        assertThat(response.users()).containsExactly("new", "old");
    }

    @Test
    @DisplayName("활성 포스트가 없으면 BusinessException이 발생한다")
    void throwsBusinessException_whenActivePostDoesNotExist() {
        // given
        given(postRepository.findActiveHeartAccessById(20L)).willReturn(Optional.empty());
        HeartQueryService service = new HeartQueryService(postRepository, heartRepository);

        // when & then
        assertThatThrownBy(() -> service.getPostHearts(1L, 20L, 1, 20))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }

    @Test
    @DisplayName("요청자가 포스트 작성자가 아니면 BusinessException이 발생한다")
    void throwsBusinessException_whenRequesterIsNotAuthor() {
        // given
        given(postRepository.findActiveHeartAccessById(20L)).willReturn(Optional.of(new PostHeartAccess(2L, 0L)));
        HeartQueryService service = new HeartQueryService(postRepository, heartRepository);

        // when & then
        assertThatThrownBy(() -> service.getPostHearts(1L, 20L, 1, 20))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(heartRepository);
    }
}
