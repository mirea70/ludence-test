package com.test.ludence.user.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserSearchKeyword 도메인 테스트")
class UserSearchKeywordTest {

    @Test
    @DisplayName("사용자 검색 이력은 검색 횟수와 최근 검색 시각을 갱신한다")
    void updatesCountAndLastSearchedAt_whenKeywordIsSearchedAgain() {
        // given
        Instant firstSearchedAt = Instant.parse("2026-06-10T10:00:00Z");
        UserSearchKeyword keyword = UserSearchKeyword.create(1L, "spring", firstSearchedAt);
        Instant nextSearchedAt = firstSearchedAt.plusSeconds(60);

        // when
        keyword.recordSearch(nextSearchedAt);

        // then
        assertThat(keyword.getSearchCount()).isEqualTo(2L);
        assertThat(keyword.getLastSearchedAt()).isEqualTo(nextSearchedAt);
    }

    @Test
    @DisplayName("검색 키워드가 공백이거나 100자를 초과하면 DomainException이 발생한다")
    void throwsDomainException_whenKeywordIsInvalid() {
        // when & then
        assertThatThrownBy(() -> UserSearchKeyword.create(1L, " ", Instant.now()))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> UserSearchKeyword.create(1L, "a".repeat(101), Instant.now()))
                .isInstanceOf(DomainException.class);
    }
}
