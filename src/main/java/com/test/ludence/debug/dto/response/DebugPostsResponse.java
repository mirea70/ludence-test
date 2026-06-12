package com.test.ludence.debug.dto.response;

import com.test.ludence.post.dto.response.PostDetailResponse;
import java.util.List;

public record DebugPostsResponse(List<PostDetailResponse> posts) {
}
