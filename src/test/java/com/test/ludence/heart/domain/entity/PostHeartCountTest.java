package com.test.ludence.heart.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostHeartCount 도메인 테스트")
class PostHeartCountTest {

    @Test
    @DisplayName("포스트 하트 수 생성 시 0으로 시작한다")
    void startsAtZero_whenCreated() {
        // when
        PostHeartCount count = PostHeartCount.create(1L);

        // then
        assertThat(count.getCount()).isZero();
    }

    @Test
    @DisplayName("하트 수를 증가시키면 1이 된다")
    void incrementsCount_whenHeartIsAdded() {
        // given
        PostHeartCount count = PostHeartCount.create(1L);

        // when
        count.increment();

        // then
        assertThat(count.getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("하트 수가 0일 때 감소시키면 DomainException이 발생한다")
    void throwsDomainException_whenZeroCountIsDecremented() {
        // given
        PostHeartCount count = PostHeartCount.create(1L);

        // when & then
        assertThatThrownBy(count::decrement)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("하트 수를 초기화하면 0이 된다")
    void resetsCount_whenPostIsDeleted() {
        // given
        PostHeartCount count = PostHeartCount.create(1L);
        count.increment();

        // when
        count.reset();

        // then
        assertThat(count.getCount()).isZero();
    }
}
