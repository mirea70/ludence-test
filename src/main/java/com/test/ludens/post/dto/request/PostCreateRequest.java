package com.test.ludens.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자입니다.")
        String title,

        @Size(max = 2000, message = "설명은 최대 2,000자입니다.")
        String description,

        @NotNull(message = "이미지는 필수입니다.")
        MultipartFile image
) {
}
