package com.test.ludens.post.service;

import static org.mockito.Mockito.verify;

import com.test.ludens.post.repository.PostRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostPhysicalDeleteService 테스트")
@ExtendWith(MockitoExtension.class)
class PostPhysicalDeleteServiceTest {

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("삭제 기준 시각이 지난 포스트 데이터를 물리 삭제한다")
    void deletesExpiredPostData() {
        // given
        Instant expiredAt = Instant.parse("2026-05-15T10:00:00Z");
        PostPhysicalDeleteService service = new PostPhysicalDeleteService(postRepository);

        // when
        service.deleteExpiredPost(1L, expiredAt);

        // then
        verify(postRepository).deleteExpiredPostData(1L, expiredAt);
    }
}
