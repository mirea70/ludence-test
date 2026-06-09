package com.test.ludence.post.domain.vo;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.post.domain.info.PostErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PostDescription(
        @Column(name = "description", length = 2000)
        String value
) {

    public PostDescription {
        if (value != null && value.length() > 2000) {
            throw new DomainException(PostErrorInfo.INVALID_DESCRIPTION);
        }
    }
}
