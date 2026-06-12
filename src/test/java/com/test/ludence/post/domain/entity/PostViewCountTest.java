package com.test.ludence.post.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostViewCount 도메인 테스트")
class PostViewCountTest {

    @Test
    @DisplayName("포스트 조회 수는 0에서 시작하고 조회할 때마다 증가한다")
    void incrementsCount_whenPostIsViewed() {
        // given
        PostViewCount count = PostViewCount.create(1L);

        // when
        count.increment();
        count.increment();

        // then
        assertThat(count.getCount()).isEqualTo(2L);
    }
}
