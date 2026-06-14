package com.test.ludens.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.storage.ImageStorage;
import com.test.ludens.post.repository.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@DisplayName("PostImageService 테스트")
@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageStorage imageStorage;

    @Test
    @DisplayName("활성 포스트의 이미지 리소스를 반환한다")
    void returnsImageResource_whenPostIsActive() {
        // given
        PostImageService service = new PostImageService(postRepository, imageStorage);
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        given(postRepository.findActiveImageKeyById(1L)).willReturn(Optional.of(IMAGE_KEY));
        given(imageStorage.get(IMAGE_KEY)).willReturn(resource);

        // when
        Resource result = service.getImage(1L);

        // then
        assertThat(result).isSameAs(resource);
        verify(imageStorage).get(IMAGE_KEY);
    }

    @Test
    @DisplayName("활성 포스트가 없으면 이미지를 조회하지 않고 BusinessException이 발생한다")
    void throwsBusinessExceptionWithoutReadingImage_whenPostIsNotActive() {
        // given
        PostImageService service = new PostImageService(postRepository, imageStorage);
        given(postRepository.findActiveImageKeyById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getImage(1L))
                .isInstanceOf(BusinessException.class);
        verify(imageStorage, never()).get(ArgumentMatchers.anyString());
    }
}
