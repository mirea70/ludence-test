package com.test.ludens.recommendation.dto.response;

import com.test.ludens.post.dto.response.PostDetailResponse;
import java.util.List;

public record RecommendationResponse(
        int limit,
        long total,
        List<PostDetailResponse> posts
) {
}
