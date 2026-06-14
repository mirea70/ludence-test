package com.test.ludens.heart.domain.vo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record HeartId(Long userId, Long postId) implements Serializable {
}
