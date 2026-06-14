package com.test.ludens.common.load;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("LoadSheddingFilter 테스트")
class LoadSheddingFilterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-13T10:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("서버가 포화 상태면 표준 에러 형식의 429를 반환한다")
    void returnsTooManyRequests_whenServerIsSaturated() throws Exception {
        // given
        ServerCapacityMonitor capacityMonitor = Mockito.mock(ServerCapacityMonitor.class);
        given(capacityMonitor.isSaturated()).willReturn(true);
        LoadSheddingFilter filter = new LoadSheddingFilter(capacityMonitor, objectMapper(), CLOCK);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader("Retry-After")).isEqualTo("1");
        assertThat(response.getContentAsString()).contains("\"code\":\"SYSTEM_002\"");
        assertThat(response.getContentAsString()).contains("\"path\":\"/posts/1\"");
        assertThat(response.getContentAsString()).contains("\"timestamp\":\"2026-06-13T10:00:00Z\"");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("서버가 포화 상태가 아니면 요청을 다음 필터로 전달한다")
    void continuesFilterChain_whenServerIsAvailable() throws Exception {
        // given
        ServerCapacityMonitor capacityMonitor = Mockito.mock(ServerCapacityMonitor.class);
        LoadSheddingFilter filter = new LoadSheddingFilter(capacityMonitor, objectMapper(), CLOCK);
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(new MockHttpServletRequest("GET", "/posts/1"), new MockHttpServletResponse(), chain);

        // then
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("헬스 체크는 서버가 포화 상태여도 요청을 다음 필터로 전달한다")
    void continuesFilterChainForHealthCheck_whenServerIsSaturated() throws Exception {
        // given
        ServerCapacityMonitor capacityMonitor = Mockito.mock(ServerCapacityMonitor.class);
        given(capacityMonitor.isSaturated()).willReturn(true);
        LoadSheddingFilter filter = new LoadSheddingFilter(capacityMonitor, objectMapper(), CLOCK);
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(new MockHttpServletRequest("GET", "/debug/health"), new MockHttpServletResponse(), chain);

        // then
        assertThat(chain.getRequest()).isNotNull();
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
