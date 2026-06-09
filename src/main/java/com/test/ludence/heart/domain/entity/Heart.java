package com.test.ludence.heart.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hearts")
public class Heart {

    @EmbeddedId
    private HeartId id;

    protected Heart() {
    }

    private Heart(HeartId id) {
        this.id = id;
    }

    public static Heart create(Long userId, Long postId) {
        validateId(userId);
        validateId(postId);
        return new Heart(new HeartId(userId, postId));
    }

    public Long getUserId() {
        return id.userId();
    }

    public Long getPostId() {
        return id.postId();
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(HeartErrorInfo.INVALID_REFERENCE_ID);
        }
    }
}
