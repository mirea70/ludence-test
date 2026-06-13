package com.test.ludence.user.domain.vo;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.UserErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record Username(
        @Column(name = "username", nullable = false, unique = true, length = 30)
        String value
) {

    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9_]{3,30}$");

    public Username {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new DomainException(UserErrorInfo.INVALID_USERNAME);
        }
    }
}
