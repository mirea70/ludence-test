package com.test.ludens.heart.dto.response;

import java.util.List;

public record HeartUserPageResponse(
        int page,
        int limit,
        long total,
        List<String> users
) {
}
