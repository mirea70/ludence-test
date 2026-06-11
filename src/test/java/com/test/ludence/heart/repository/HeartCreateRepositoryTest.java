package com.test.ludence.heart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("하트 생성 저장소 쿼리 테스트")
class HeartCreateRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("포스트 하트 집계 행을 수정용 잠금으로 조회한다")
    void findsPostHeartCountForUpdate() {
        // given
        postHeartCountRepository.save(PostHeartCount.create(10L));
        entityManager.flush();
        entityManager.clear();

        // when
        PostHeartCount heartCount = postHeartCountRepository.findByIdForUpdate(10L).orElseThrow();

        // then
        assertThat(heartCount.getPostId()).isEqualTo(10L);
    }
}
