package com.test.ludence.user.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.recommendation.domain.info.RecommendationErrorInfo;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
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
        name = "user_search_keywords",
        indexes = {
                @Index(
                        name = "idx_user_search_keywords_user_last_searched",
                        columnList = "user_id, last_searched_at DESC"
                ),
                @Index(
                        name = "idx_user_search_keywords_last_searched_user",
                        columnList = "last_searched_at DESC, user_id"
                )
        }
)
public class UserSearchKeyword {

    @EmbeddedId
    private UserSearchKeywordId id;

    @Column(nullable = false)
    private long searchCount;

    @Column(nullable = false)
    private Instant lastSearchedAt;

    protected UserSearchKeyword() {
    }

    private UserSearchKeyword(UserSearchKeywordId id, Instant lastSearchedAt) {
        this.id = id;
        this.searchCount = 1;
        this.lastSearchedAt = lastSearchedAt;
    }

    public static UserSearchKeyword create(Long userId, String keyword, Instant searchedAt) {
        validateId(userId);
        validateKeyword(keyword);
        validateTime(searchedAt);
        return new UserSearchKeyword(new UserSearchKeywordId(userId, keyword), searchedAt);
    }

    public void recordSearch(Instant searchedAt) {
        validateTime(searchedAt);
        validateNotBefore(searchedAt, lastSearchedAt);
        searchCount++;
        lastSearchedAt = searchedAt;
    }

    public Long getUserId() {
        return id.userId();
    }

    public String getKeyword() {
        return id.keyword();
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(RecommendationErrorInfo.INVALID_REFERENCE_ID);
        }
    }

    private static void validateKeyword(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() > 100) {
            throw new DomainException(RecommendationErrorInfo.INVALID_KEYWORD);
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
