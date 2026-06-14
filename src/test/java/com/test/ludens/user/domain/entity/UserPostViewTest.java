package com.test.ludens.user.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserPostView 도메인 테스트")
class UserPostViewTest {

    @Test
    @DisplayName("사용자 포스트 조회 이력은 조회 횟수와 최근 조회 시각을 갱신한다")
    void updatesCountAndLastViewedAt_whenPostIsViewedAgain() {
        // given
        Instant firstViewedAt = Instant.parse("2026-06-10T10:00:00Z");
        UserPostView view = UserPostView.create(1L, 2L, firstViewedAt);
        Instant nextViewedAt = firstViewedAt.plusSeconds(60);

        // when
        view.recordView(nextViewedAt);

        // then
        assertThat(view.getViewCount()).isEqualTo(2L);
        assertThat(view.getLastViewedAt()).isEqualTo(nextViewedAt);
    }

    @Test
    @DisplayName("기존 최근 조회 시각보다 과거 시각으로 조회 이력을 갱신할 수 없다")
    void throwsDomainException_whenViewedAtMovesBackward() {
        // given
        Instant viewedAt = Instant.parse("2026-06-10T10:00:00Z");
        UserPostView view = UserPostView.create(1L, 2L, viewedAt);

        // when & then
        assertThatThrownBy(() -> view.recordView(viewedAt.minusSeconds(1)))
                .isInstanceOf(DomainException.class);
    }
}
