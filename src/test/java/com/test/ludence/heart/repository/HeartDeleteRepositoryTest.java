package com.test.ludence.heart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("하트 삭제 저장소 쿼리 테스트")
class HeartDeleteRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("사용자와 포스트 ID가 일치하는 하트만 삭제한다")
    void deletesHeartByUserIdAndPostId() {
        // given
        heartRepository.save(Heart.create(1L, 10L));
        heartRepository.save(Heart.create(2L, 10L));
        entityManager.flush();
        entityManager.clear();

        // when
        long deletedCount = heartRepository.deleteByUserIdAndPostId(1L, 10L);

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(heartRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("일치하는 하트가 없으면 삭제 건수 0을 반환한다")
    void returnsZero_whenHeartDoesNotExist() {
        // when
        long deletedCount = heartRepository.deleteByUserIdAndPostId(1L, 10L);

        // then
        assertThat(deletedCount).isZero();
    }
}
