package com.test.ludens.post.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostDescription 테스트")
class PostDescriptionTest {

    @Test
    @DisplayName("설명이 null이면 선택값으로 생성된다")
    void createsPostDescription_whenValueIsNull() {
        // when
        PostDescription description = new PostDescription(null);

        // then
        assertThat(description.value()).isNull();
    }

    @Test
    @DisplayName("설명이 2000자를 초과하면 DomainException이 발생한다")
    void throwsDomainException_whenDescriptionExceedsMaximumLength() {
        // when & then
        assertThatThrownBy(() -> new PostDescription("a".repeat(2001)))
                .isInstanceOf(DomainException.class);
    }
}
