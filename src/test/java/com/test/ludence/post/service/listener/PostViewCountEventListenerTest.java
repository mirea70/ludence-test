package com.test.ludence.post.service.listener;

import static org.mockito.Mockito.verify;

import com.test.ludence.post.domain.event.PostViewedEvent;
import com.test.ludence.post.service.PostViewCountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostViewCountEventListener 테스트")
@ExtendWith(MockitoExtension.class)
class PostViewCountEventListenerTest {

    @Mock
    private PostViewCountService postViewCountService;

    @Test
    @DisplayName("포스트 조회 이벤트를 조회 수 기록 서비스로 전달한다")
    void delegatesPostViewedEvent() {
        // given
        PostViewCountEventListener listener = new PostViewCountEventListener(postViewCountService);

        // when
        listener.handle(new PostViewedEvent(10L, null));

        // then
        verify(postViewCountService).recordView(10L);
    }
}
