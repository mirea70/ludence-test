package com.test.ludence.post.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.recommendation.domain.info.RecommendationErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "post_view_counts")
public class PostViewCount {

    @Id
    private Long postId;

    @Column(name = "count", nullable = false)
    private long count;

    protected PostViewCount() {
    }

    private PostViewCount(Long postId) {
        this.postId = postId;
    }

    public static PostViewCount create(Long postId) {
        validateId(postId);
        return new PostViewCount(postId);
    }

    public void increment() {
        count++;
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(RecommendationErrorInfo.INVALID_REFERENCE_ID);
        }
    }
}
