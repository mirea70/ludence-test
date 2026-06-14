package com.test.ludens.user.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludens.post.domain.event.PostViewedEvent;
import com.test.ludens.search.domain.event.PostSearchedEvent;
import com.test.ludens.user.service.UserActivityService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;

@DisplayName("UserActivityEventListener 테스트")
@ExtendWith(MockitoExtension.class)
class UserActivityEventListenerTest {

    @Mock
    private UserActivityService userActivityService;

    @Test
    @DisplayName("사용자 행동 이벤트를 추천 행동 기록 서비스로 전달한다")
    void delegatesUserActivityEvents() {
        // given
        UserActivityEventListener listener = new UserActivityEventListener(userActivityService);

        // when
        listener.handle(new PostViewedEvent(10L, 1L));
        listener.handle(new PostSearchedEvent(1L, "spring"));

        // then
        verify(userActivityService).recordPostView(10L, 1L);
        verify(userActivityService).recordSearch(1L, "spring");
    }

    @Test
    @DisplayName("비로그인 포스트 조회 이벤트는 사용자 행동으로 기록하지 않는다")
    void ignoresPostViewedEvent_whenUserIsAnonymous() {
        // given
        UserActivityEventListener listener = new UserActivityEventListener(userActivityService);

        // when
        listener.handle(new PostViewedEvent(10L, null));

        // then
        verifyNoInteractions(userActivityService);
    }

    @Test
    @DisplayName("사용자 활동 전용 실행기에서 이벤트를 처리한다")
    void handlesEventsWithUserActivityExecutor() throws NoSuchMethodException {
        // when
        Method postViewedHandler = UserActivityEventListener.class.getMethod("handle", PostViewedEvent.class);
        Method postSearchedHandler = UserActivityEventListener.class.getMethod("handle", PostSearchedEvent.class);

        // then
        assertThat(postViewedHandler.getAnnotation(Async.class).value()).isEqualTo("userActivityTaskExecutor");
        assertThat(postSearchedHandler.getAnnotation(Async.class).value()).isEqualTo("userActivityTaskExecutor");
    }
}
