package com.test.ludens.user.dto.response;

import java.time.Instant;

public record UserDetailResponse(
        String username,
        long postCount,
        Instant createdAt
) {
}
