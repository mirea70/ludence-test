package com.test.ludence.post.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.post.repository.PostViewCountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostViewCountService 테스트")
@ExtendWith(MockitoExtension.class)
class PostViewCountServiceTest {

    @Mock
    private PostViewCountRepository postViewCountRepository;

    @Test
    @DisplayName("포스트 조회를 기록하면 조회 수를 증가시킨다")
    void incrementsViewCount_whenPostViewIsRecorded() {
        // given
        given(postViewCountRepository.increment(10L)).willReturn(1L);
        PostViewCountService service = new PostViewCountService(postViewCountRepository);

        // when
        service.recordView(10L);

        // then
        verify(postViewCountRepository).increment(10L);
    }
}
