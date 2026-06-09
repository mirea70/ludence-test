package com.test.ludence.heart.domain.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record HeartId(Long userId, Long postId) implements Serializable {
}
