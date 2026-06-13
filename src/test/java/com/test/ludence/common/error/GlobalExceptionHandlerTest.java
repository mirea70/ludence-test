package com.test.ludence.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.test.ludence.common.error.handler.GlobalExceptionHandler;
import com.test.ludence.common.error.response.ErrorResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private static final Instant NOW = Instant.parse("2026-06-13T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    @DisplayName("비동기 실행기 작업이 거부되면 429를 반환한다")
    void returnsTooManyRequests_whenAsyncTaskIsRejected() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts/1");
        MultipartProperties multipartProperties = new MultipartProperties();

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(CLOCK, multipartProperties)
                .handleTaskRejectedException(new TaskRejectedException("rejected"), request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("1");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SYSTEM_002");
        assertThat(response.getBody().timestamp()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("업로드 크기 초과 시 413을 반환한다")
    void returnsPayloadTooLarge_whenUploadSizeExceeded() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts");
        MultipartProperties multipartProperties = new MultipartProperties();
        DataSize maxFileSize = DataSize.ofMegabytes(2);
        multipartProperties.setMaxFileSize(maxFileSize);

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(CLOCK, multipartProperties)
                .handleMaxUploadSizeExceededException(mock(MaxUploadSizeExceededException.class), request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SYSTEM_003");
        assertThat(response.getBody().timestamp()).isEqualTo(NOW);
        assertThat(response.getBody().details()).containsEntry("업로드 파일 최대 허용 크기 : ", maxFileSize.toString());
    }
}
