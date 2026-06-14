package com.test.ludens.debug.dto.response;

import com.test.ludens.post.dto.response.PostDetailResponse;
import java.util.List;

public record DebugPostsResponse(List<PostDetailResponse> posts) {
}
