package com.test.ludence.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.test.ludence.common.error.handler.GlobalExceptionHandler;
import com.test.ludence.common.error.response.ErrorResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.unit.DataSize;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
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
        assertThat(response.getBody().details()).containsEntry("maxFileSize", maxFileSize.toString());
    }

    @Test
    @DisplayName("필수 헤더가 누락되면 400을 반환한다")
    void returnsBadRequest_whenRequestHeaderIsMissing() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts/1");
        MultipartProperties multipartProperties = new MultipartProperties();
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        given(exception.getHeaderName()).willReturn("Authorization");

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(CLOCK, multipartProperties)
                .handleMissingRequestHeaderException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON_003");
        assertThat(response.getBody().details()).containsEntry("header", "Authorization");
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드면 405와 Allow 헤더를 반환한다")
    void returnsMethodNotAllowed_whenHttpMethodIsUnsupported() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts/1");
        MultipartProperties multipartProperties = new MultipartProperties();
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        given(exception.getSupportedHttpMethods()).willReturn(Set.of(HttpMethod.GET));

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(CLOCK, multipartProperties)
                .handleHttpRequestMethodNotSupportedException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getHeaders().getAllow()).contains(HttpMethod.GET);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON_004");
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type이면 415를 반환한다")
    void returnsUnsupportedMediaType_whenContentTypeIsUnsupported() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts");
        MultipartProperties multipartProperties = new MultipartProperties();

        // when
        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(CLOCK, multipartProperties)
                .handleHttpMediaTypeNotSupportedException(mock(HttpMediaTypeNotSupportedException.class), request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON_005");
    }
}
