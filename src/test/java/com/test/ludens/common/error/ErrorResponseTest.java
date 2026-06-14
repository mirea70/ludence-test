package com.test.ludens.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.test.ludens.common.error.response.ErrorResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ErrorResponse 테스트")
class ErrorResponseTest {

    private static final Instant NOW = Instant.parse("2026-06-13T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    @DisplayName("주입된 Clock의 UTC Instant를 타임스탬프로 사용한다")
    void usesUtcInstantFromClock_asTimestamp() {
        // when
        ErrorResponse response = new ErrorResponse(CLOCK, "COMMON_001", "message", "/posts");

        // then
        assertThat(response.timestamp()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("타임스탬프를 UTC ISO-8601 형식으로 직렬화한다")
    void serializesTimestamp_asUtcIso8601() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ErrorResponse response = new ErrorResponse(CLOCK, "COMMON_001", "message", "/posts");

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        assertThat(json).contains("\"timestamp\":\"2026-06-13T10:00:00Z\"");
    }
}
