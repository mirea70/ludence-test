package com.test.ludens.heart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("하트 생성 저장소 쿼리 테스트")
class HeartCreateRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("포스트 하트 집계값을 원자적으로 증가시킨다")
    void increasesPostHeartCount() {
        // given
        postHeartCountRepository.save(PostHeartCount.create(10L));
        entityManager.flush();
        entityManager.clear();

        // when
        long updatedCount = postHeartCountRepository.increase(10L);
        entityManager.clear();

        // then
        assertThat(updatedCount).isEqualTo(1L);
        assertThat(postHeartCountRepository.findById(10L).orElseThrow().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("하트 집계 행이 없으면 증가 건수 0을 반환한다")
    void returnsZero_whenPostHeartCountDoesNotExist() {
        // when
        long updatedCount = postHeartCountRepository.increase(10L);

        // then
        assertThat(updatedCount).isZero();
    }
}
