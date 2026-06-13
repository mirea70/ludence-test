package com.test.ludence.post.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.test.ludence.post.domain.event.PostViewedEvent;
import com.test.ludence.post.service.PostViewCountService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;

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

    @Test
    @DisplayName("조회수 전용 실행기에서 이벤트를 처리한다")
    void handlesEventWithPostViewCountExecutor() throws NoSuchMethodException {
        // when
        Method method = PostViewCountEventListener.class.getMethod("handle", PostViewedEvent.class);

        // then
        assertThat(method.getAnnotation(Async.class).value()).isEqualTo("postViewCountTaskExecutor");
    }
}
