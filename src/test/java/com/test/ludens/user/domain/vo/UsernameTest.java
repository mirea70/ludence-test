package com.test.ludens.user.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludens.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Username 테스트")
class UsernameTest {

    @Test
    @DisplayName("영문 소문자 숫자 밑줄로 구성된 3자 이상 username이면 생성된다")
    void createsUsername_whenValueIsValid() {
        // when
        Username username = new Username("sunny_01");

        // then
        assertThat(username.value()).isEqualTo("sunny_01");
    }

    @Test
    @DisplayName("username이 3자보다 짧으면 DomainException이 발생한다")
    void throwsDomainException_whenUsernameIsTooShort() {
        // when & then
        assertThatThrownBy(() -> new Username("ab"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("username에 허용되지 않은 문자가 있으면 DomainException이 발생한다")
    void throwsDomainException_whenUsernameContainsInvalidCharacter() {
        // when & then
        assertThatThrownBy(() -> new Username("Sunny!"))
                .isInstanceOf(DomainException.class);
    }
}
