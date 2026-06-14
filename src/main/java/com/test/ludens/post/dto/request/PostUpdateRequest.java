package com.test.ludens.post.dto.request;

import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Size(max = 100, message = "제목은 최대 100자입니다.")
        String title,

        @Size(max = 2000, message = "설명은 최대 2,000자입니다.")
        String description
) {
}
