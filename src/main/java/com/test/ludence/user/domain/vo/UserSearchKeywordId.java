package com.test.ludence.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record UserSearchKeywordId(
        Long userId,
        @Column(length = 100)
        String keyword
) implements Serializable {
}
