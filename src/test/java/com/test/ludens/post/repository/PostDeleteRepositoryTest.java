package com.test.ludens.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("포스트 삭제 저장소 쿼리 테스트")
class PostDeleteRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("포스트 ID에 해당하는 하트를 모두 삭제한다")
    void deletesHeartsByPostId() {
        // given
        heartRepository.save(Heart.create(1L, 10L));
        heartRepository.save(Heart.create(2L, 10L));
        heartRepository.save(Heart.create(1L, 20L));
        entityManager.flush();
        entityManager.clear();

        // when
        long deletedCount = heartRepository.deleteByPostId(10L);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(deletedCount).isEqualTo(2);
        assertThat(heartRepository.count()).isEqualTo(1);
    }
}
