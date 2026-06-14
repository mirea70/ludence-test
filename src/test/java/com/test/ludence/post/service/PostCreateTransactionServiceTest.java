package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.storage.ImageStorage;
import com.test.ludence.common.storage.StagedImage;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.domain.entity.PostViewCount;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.post.repository.PostViewCountRepository;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("PostCreateTransactionService 테스트")
@ExtendWith(MockitoExtension.class)
class PostCreateTransactionServiceTest {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-10T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostHeartCountRepository postHeartCountRepository;

    @Mock
    private PostViewCountRepository postViewCountRepository;

    @Mock
    private ImageStorage imageStorage;

    @Test
    @DisplayName("활성 작성자를 잠근 후 포스트와 하트 수를 저장하고 이미지를 확정한다")
    void createsPostAndCommitsImage_whenAuthorIsActive() {
        // given
        PostCreateTransactionService service = createService();
        User user = User.create("sunny", "encoded-password", clock.instant());
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(postRepository.saveAndFlush(ArgumentMatchers.any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 10L);
            return post;
        });

        // when
        PostIdResponse response = service.createPost(1L, "title", "description", stagedImage);

        // then
        assertThat(response.id()).isEqualTo(10L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).saveAndFlush(postCaptor.capture());
        assertThat(postCaptor.getValue().getImageKey()).isEqualTo(IMAGE_KEY);
        ArgumentCaptor<PostHeartCount> countCaptor = ArgumentCaptor.forClass(PostHeartCount.class);
        verify(postHeartCountRepository).saveAndFlush(countCaptor.capture());
        assertThat(countCaptor.getValue().getPostId()).isEqualTo(10L);
        ArgumentCaptor<PostViewCount> viewCountCaptor = ArgumentCaptor.forClass(PostViewCount.class);
        verify(postViewCountRepository).saveAndFlush(viewCountCaptor.capture());
        assertThat(viewCountCaptor.getValue().getPostId()).isEqualTo(10L);
        verify(imageStorage).commit(stagedImage);
    }

    @Test
    @DisplayName("활성 작성자가 아니면 포스트를 저장하거나 이미지를 확정하지 않는다")
    void throwsBusinessExceptionWithoutSavingPost_whenAuthorIsNotActive() {
        // given
        PostCreateTransactionService service = createService();
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        given(userRepository.findActiveByIdForUpdate(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.createPost(1L, "title", "description", stagedImage))
                .isInstanceOf(BusinessException.class);
        verify(postRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(imageStorage, never()).commit(stagedImage);
    }

    private PostCreateTransactionService createService() {
        return new PostCreateTransactionService(
                userRepository,
                postRepository,
                postHeartCountRepository,
                postViewCountRepository,
                imageStorage,
                clock
        );
    }
}
