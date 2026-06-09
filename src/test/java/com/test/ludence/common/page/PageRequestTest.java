package com.test.ludence.common.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PageRequest 테스트")
class PageRequestTest {

    @Test
    @DisplayName("페이지 번호와 크기가 유효하면 0부터 시작하는 offset을 계산한다")
    void calculatesOffset_whenPageAndLimitAreValid() {
        // given
        PageRequest request = new PageRequest(2, 20);

        // when
        long offset = request.offset();

        // then
        assertThat(offset).isEqualTo(20);
    }

    @Test
    @DisplayName("페이지 번호가 1보다 작으면 DomainException이 발생한다")
    void throwsDomainException_whenPageIsLessThanOne() {
        // when & then
        assertThatThrownBy(() -> new PageRequest(0, 20))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("페이지 크기가 최대값을 초과하면 DomainException이 발생한다")
    void throwsDomainException_whenLimitExceedsMaximum() {
        // when & then
        assertThatThrownBy(() -> new PageRequest(1, 101))
                .isInstanceOf(DomainException.class);
    }
}
