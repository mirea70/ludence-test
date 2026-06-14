package com.test.ludens.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AsyncTaskRejectionHandler 테스트")
class AsyncTaskRejectionHandlerTest {

    @Test
    @DisplayName("비동기 작업이 거절되어도 요청 스레드에 예외를 전파하지 않는다")
    void doesNotPropagateException_whenAsyncTaskIsRejected() {
        // given
        AsyncTaskRejectionHandler handler = new AsyncTaskRejectionHandler("user-activity-");
        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        given(executor.getQueue()).willReturn(new LinkedBlockingQueue<>());

        // when & then
        assertThatCode(() -> handler.rejectedExecution(() -> {
        }, executor)).doesNotThrowAnyException();
    }
}
