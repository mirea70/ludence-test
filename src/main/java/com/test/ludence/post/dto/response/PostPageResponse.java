package com.test.ludence.post.dto.response;

import java.util.List;

public record PostPageResponse(
        int page,
        int limit,
        long total,
        List<PostDetailResponse> posts
) {
}
