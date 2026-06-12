package com.test.ludence.debug.dto.response;

import com.test.ludence.user.dto.response.UserDetailResponse;
import java.util.List;

public record DebugUsersResponse(List<UserDetailResponse> users) {
}
