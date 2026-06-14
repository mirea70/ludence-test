package com.test.ludens.post.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostTitle 테스트")
class PostTitleTest {

    @Test
    @DisplayName("공백이 아닌 100자 이하 제목이면 생성된다")
    void createsPostTitle_whenValueIsValid() {
        // when
        PostTitle title = new PostTitle("my post");

        // then
        assertThat(title.value()).isEqualTo("my post");
    }

    @Test
    @DisplayName("제목이 공백이면 DomainException이 발생한다")
    void throwsDomainException_whenTitleIsBlank() {
        // when & then
        assertThatThrownBy(() -> new PostTitle(" "))
                .isInstanceOf(DomainException.class);
    }
}
