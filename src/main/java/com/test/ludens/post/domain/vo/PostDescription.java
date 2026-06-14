package com.test.ludens.post.domain.vo;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.PostErrorInfo;
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
