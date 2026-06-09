package com.test.ludence.heart.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "post_heart_counts")
public class PostHeartCount {

    @Id
    private Long postId;

    @Column(name = "count", nullable = false)
    private long count;

    protected PostHeartCount() {
    }

    private PostHeartCount(Long postId) {
        this.postId = postId;
    }

    public static PostHeartCount create(Long postId) {
        validateId(postId);
        return new PostHeartCount(postId);
    }

    public void increment() {
        count++;
    }

    public void decrement() {
        if (count == 0) {
            throw new DomainException(HeartErrorInfo.INVALID_COUNT);
        }
        count--;
    }

    public void reset() {
        count = 0;
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(HeartErrorInfo.INVALID_REFERENCE_ID);
        }
    }
}
