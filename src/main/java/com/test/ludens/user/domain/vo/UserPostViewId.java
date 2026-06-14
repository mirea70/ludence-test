package com.test.ludens.user.domain.vo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record UserPostViewId(Long userId, Long postId) implements Serializable {
}
