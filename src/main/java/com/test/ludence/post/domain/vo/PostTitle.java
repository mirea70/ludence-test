package com.test.ludence.post.domain.vo;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.PostErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PostTitle(
        @Column(name = "title", nullable = false, length = 100)
        String value
) {

    public PostTitle {
        if (value == null || value.isBlank() || value.length() > 100) {
            throw new DomainException(PostErrorInfo.INVALID_TITLE);
        }
    }
}
