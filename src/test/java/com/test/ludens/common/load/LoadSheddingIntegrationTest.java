package com.test.ludens.common.load;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.test.ludens.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("로드 셰딩 통합 테스트")
class LoadSheddingIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    private ServerCapacityMonitor capacityMonitor;

    @Test
    @DisplayName("포화 상태에서는 JWT 인증보다 먼저 표준 에러 형식의 429를 반환한다")
    void returnsTooManyRequestsBeforeAuthentication_whenServerIsSaturated() throws Exception {
        // given
        given(capacityMonitor.isSaturated()).willReturn(true);

        // when & then
        mockMvc.perform(delete("/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "1"))
                .andExpect(jsonPath("$.code").value("SYSTEM_002"))
                .andExpect(jsonPath("$.path").value("/auth/me"));
    }

    @Test
    @DisplayName("포화 상태에서도 헬스 체크는 200을 반환한다")
    void returnsOkForHealthCheck_whenServerIsSaturated() throws Exception {
        // given
        given(capacityMonitor.isSaturated()).willReturn(true);

        // when & then
        mockMvc.perform(get("/debug/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}
