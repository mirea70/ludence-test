package com.test.ludens.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9_]{3,30}$", message = "username 형식이 유효하지 않습니다.")
        String username,

        @NotBlank
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
        String password
) {
}
