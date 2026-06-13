package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import com.test.ludence.common.storage.ImageStorage;
import com.test.ludence.common.storage.StagedImage;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("PostCreateService 테스트")
@ExtendWith(MockitoExtension.class)
class PostCreateServiceTest {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Mock
    private PostCreateTransactionService transactionService;

    @Mock
    private ImageStorage imageStorage;

    @Test
    @DisplayName("이미지를 스테이징한 후 트랜잭션에서 포스트를 생성한다")
    void stagesImageBeforeCreatingPostInTransaction() {
        // given
        PostCreateService service = new PostCreateService(transactionService, imageStorage);
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        PostCreateRequest request = createRequest();
        given(imageStorage.stage(org.mockito.ArgumentMatchers.any())).willReturn(stagedImage);
        given(transactionService.createPost(1L, request.title(), request.description(), stagedImage))
                .willReturn(new PostIdResponse(10L));

        // when
        PostIdResponse response = service.createPost(1L, request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        InOrder inOrder = inOrder(imageStorage, transactionService);
        inOrder.verify(imageStorage).stage(org.mockito.ArgumentMatchers.any());
        inOrder.verify(transactionService).createPost(1L, request.title(), request.description(), stagedImage);
    }

    @Test
    @DisplayName("트랜잭션 포스트 생성에 실패하면 스테이징 이미지를 제거한다")
    void discardsStagedImage_whenTransactionFails() {
        // given
        PostCreateService service = new PostCreateService(transactionService, imageStorage);
        StagedImage stagedImage = new StagedImage(IMAGE_KEY, Path.of("temporary"));
        PostCreateRequest request = createRequest();
        given(imageStorage.stage(org.mockito.ArgumentMatchers.any())).willReturn(stagedImage);
        given(transactionService.createPost(1L, request.title(), request.description(), stagedImage))
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
