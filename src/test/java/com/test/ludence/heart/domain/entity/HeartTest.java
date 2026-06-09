package com.test.ludence.heart.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Heart 도메인 테스트")
class HeartTest {

    @Test
    @DisplayName("유효한 사용자 ID와 포스트 ID로 하트를 생성한다")
    void createsHeart_whenIdsAreValid() {
        // when
        Heart heart = Heart.create(1L, 2L);

        // then
        assertThat(heart.getUserId()).isEqualTo(1L);
        assertThat(heart.getPostId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 ID가 유효하지 않으면 DomainException이 발생한다")
    void throwsDomainException_whenUserIdIsInvalid() {
        // when & then
        assertThatThrownBy(() -> Heart.create(0L, 2L))
                .isInstanceOf(DomainException.class);
    }
}
