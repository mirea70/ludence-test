package com.test.ludens.post.domain.vo;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.PostErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record PostImageKey(
        @Column(name = "image_key", nullable = false, unique = true, updatable = false, length = 40)
        String value
) {

    private static final Pattern FORMAT = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.png"
    );

    public PostImageKey {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new DomainException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }
}
