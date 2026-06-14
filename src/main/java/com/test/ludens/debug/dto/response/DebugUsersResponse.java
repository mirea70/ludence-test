package com.test.ludens.debug.dto.response;

import com.test.ludens.user.dto.response.UserDetailResponse;
import java.util.List;

public record DebugUsersResponse(List<UserDetailResponse> users) {
}
