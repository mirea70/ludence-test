package com.test.ludence.recommendation.dto.response;

import com.test.ludence.post.dto.response.PostDetailResponse;
import java.util.List;

public record RecommendationResponse(
        int limit,
        long total,
        List<PostDetailResponse> posts
) {
}
