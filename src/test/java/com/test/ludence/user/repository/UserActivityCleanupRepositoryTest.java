package com.test.ludence.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.support.JpaTestSupport;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserPostViewId;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("사용자 행동 이력 정리 Repository 테스트")
class UserActivityCleanupRepositoryTest extends JpaTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");
    private static final Instant EXPIRED_AT = NOW.minusSeconds(604800);

    @Test
    @DisplayName("7일이 지난 조회와 검색 이력 ID를 오래된 순서로 조회한다")
    void findsExpiredActivityIdsInOldestOrder() {
        // given
        userPostViewRepository.saveAll(List.of(
                UserPostView.create(1L, 1L, EXPIRED_AT.minusSeconds(2)),
                UserPostView.create(1L, 2L, EXPIRED_AT.minusSeconds(1)),
                UserPostView.create(1L, 3L, EXPIRED_AT)
        ));
        userSearchKeywordRepository.saveAll(List.of(
                UserSearchKeyword.create(1L, "oldest", EXPIRED_AT.minusSeconds(2)),
                UserSearchKeyword.create(1L, "old", EXPIRED_AT.minusSeconds(1)),
                UserSearchKeyword.create(1L, "current", EXPIRED_AT)
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        List<UserPostViewId> viewIds = userPostViewRepository.findIdsLastViewedBefore(EXPIRED_AT);
        List<UserSearchKeywordId> keywordIds = userSearchKeywordRepository.findIdsLastSearchedBefore(EXPIRED_AT);

        // then
        assertThat(viewIds).containsExactly(
                new UserPostViewId(1L, 1L),
                new UserPostViewId(1L, 2L)
        );
        assertThat(keywordIds).containsExactly(
                new UserSearchKeywordId(1L, "oldest"),
                new UserSearchKeywordId(1L, "old")
        );
    }
}
