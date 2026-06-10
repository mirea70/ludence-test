package com.test.ludence.post.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostImageKey 도메인 테스트")
class PostImageKeyTest {

    @Test
    @DisplayName("UUID PNG 파일명은 이미지 키로 생성할 수 있다")
    void createsImageKey_whenValueIsUuidPngFileName() {
        // when
        PostImageKey imageKey = new PostImageKey("550e8400-e29b-41d4-a716-446655440000.png");

        // then
        assertThat(imageKey.value()).isEqualTo("550e8400-e29b-41d4-a716-446655440000.png");
    }

    @Test
    @DisplayName("경로가 포함된 값은 이미지 키로 생성할 수 없다")
    void throwsDomainException_whenValueContainsPath() {
        // when & then
        assertThatThrownBy(() -> new PostImageKey("../550e8400-e29b-41d4-a716-446655440000.png"))
                .isInstanceOf(DomainException.class);
    }
}
