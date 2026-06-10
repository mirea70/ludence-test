package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.common.storage.ImageStorage;
import com.test.ludence.common.storage.StagedImage;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("PostCreateService 테스트")
@ExtendWith(MockitoExtension.class)
class PostCreateServiceTest {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-10T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Mock
    private ImageStorage imageStorage;

    @Test
    @DisplayName("유효한 요청이면 포스트와 하트 수를 저장하고 이미지를 포스트 ID로 확정한다")
    void createsPostAndCommitsImage_whenRequestIsValid() throws Exception {
        // given
        PostCreateService service = new PostCreateService(
                userRepository, postRepository, postHeartCountRepository, imageStorage, clock
        );
        User user = User.create("sunny", "encoded-password", clock.instant());
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        PostCreateRequest request = createRequest();
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(imageStorage.stage(org.mockito.ArgumentMatchers.any())).willReturn(stagedImage);
        given(postRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 10L);
            return post;
        });

        // when
        PostIdResponse response = service.createPost(1L, request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).saveAndFlush(postCaptor.capture());
        assertThat(postCaptor.getValue().getImageKey()).isEqualTo(IMAGE_KEY);
        ArgumentCaptor<PostHeartCount> countCaptor = ArgumentCaptor.forClass(PostHeartCount.class);
        verify(postHeartCountRepository).saveAndFlush(countCaptor.capture());
        assertThat(countCaptor.getValue().getPostId()).isEqualTo(10L);
        assertThat(countCaptor.getValue().getCount()).isZero();
        verify(imageStorage).commit(stagedImage);
    }

    @Test
    @DisplayName("활성 회원이 아니면 이미지를 저장하지 않고 예외가 발생한다")
    void throwsBusinessExceptionWithoutStagingImage_whenUserIsNotActive() {
        // given
        PostCreateService service = new PostCreateService(
                userRepository, postRepository, postHeartCountRepository, imageStorage, clock
        );
        PostCreateRequest request = createRequest();
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.createPost(1L, request))
                .isInstanceOf(com.test.ludence.common.error.exception.BusinessException.class);
        verify(imageStorage, never()).stage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("포스트 저장에 실패하면 임시 이미지를 제거한다")
    void discardsStagedImage_whenPostSaveFails() throws Exception {
        // given
        PostCreateService service = new PostCreateService(
                userRepository, postRepository, postHeartCountRepository, imageStorage, clock
        );
        User user = User.create("sunny", "encoded-password", clock.instant());
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        PostCreateRequest request = createRequest();
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(imageStorage.stage(org.mockito.ArgumentMatchers.any())).willReturn(stagedImage);
        given(postRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Post.class)))
                .willThrow(new IllegalStateException("save failed"));

        // when & then
        assertThatThrownBy(() -> service.createPost(1L, request))
                .isInstanceOf(IllegalStateException.class);
        verify(imageStorage).discard(stagedImage);
    }

    private PostCreateRequest createRequest() {
        return new PostCreateRequest(
                "title",
                "description",
                new MockMultipartFile("image", "image.png", "image/png", new byte[10])
        );
    }
}
