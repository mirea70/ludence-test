package com.test.ludence.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.common.error.handler.GlobalExceptionHandler;
import com.test.ludence.common.error.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("비동기 실행기가 작업을 거부하면 429를 반환한다")
    void returnsTooManyRequests_whenAsyncTaskIsRejected() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts/1");

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler().handleTaskRejectedException(
                new TaskRejectedException("rejected"),
                request
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("1");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SYSTEM_002");
    }
}
