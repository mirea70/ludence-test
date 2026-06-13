package com.test.ludence.user.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.RecommendationErrorInfo;
import com.test.ludence.user.domain.vo.UserPostViewId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "user_post_views",
        indexes = {
                @Index(name = "idx_user_post_views_user_last_viewed", columnList = "user_id, last_viewed_at DESC"),
                @Index(name = "idx_user_post_views_last_viewed_post", columnList = "last_viewed_at DESC, post_id, user_id")
        }
)
public class UserPostView {

    @EmbeddedId
    private UserPostViewId id;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private Instant lastViewedAt;

    protected UserPostView() {
    }

    private UserPostView(UserPostViewId id, Instant lastViewedAt) {
        this.id = id;
        this.viewCount = 1;
        this.lastViewedAt = lastViewedAt;
    }

    public static UserPostView create(Long userId, Long postId, Instant viewedAt) {
        validateId(userId);
        validateId(postId);
        validateTime(viewedAt);
        return new UserPostView(new UserPostViewId(userId, postId), viewedAt);
    }

    public void recordView(Instant viewedAt) {
        validateTime(viewedAt);
        validateNotBefore(viewedAt, lastViewedAt);
        viewCount++;
        lastViewedAt = viewedAt;
    }

    public Long getUserId() {
        return id.userId();
    }

    public Long getPostId() {
        return id.postId();
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(RecommendationErrorInfo.INVALID_REFERENCE_ID);
        }
    }

    private static void validateTime(Instant time) {
        if (time == null) {
            throw new DomainException(RecommendationErrorInfo.INVALID_TIME);
        }
    }

    private static void validateNotBefore(Instant time, Instant previousTime) {
        if (time.isBefore(previousTime)) {
            throw new DomainException(RecommendationErrorInfo.INVALID_TIME);
        }
    }
}
