package com.test.ludens.heart.domain.entity;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.HeartErrorInfo;
import com.test.ludens.heart.domain.vo.HeartId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
        name = "hearts",
        indexes = @Index(name = "idx_hearts_post_user", columnList = "post_id, user_id")
)
public class Heart implements Persistable<HeartId> {

    @EmbeddedId
    private HeartId id;

    @Transient
    private boolean isNew = true;

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

    @Override
    public HeartId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    private void markNotNew() {
        isNew = false;
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(HeartErrorInfo.INVALID_REFERENCE_ID);
        }
    }
}
