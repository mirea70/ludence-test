package com.test.ludence.post.dto.response;

import java.time.Instant;

public record PostDetailResponse(
        Long id,
        String title,
        String description,
        Instant createdAt,
        Instant editedAt,
        String username,
        long heartCount,
        boolean hearted
) {
}
