package com.test.ludence.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartCountByPostId;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.support.JpaTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("회원 탈퇴 저장소 쿼리 테스트")
class WithdrawalRepositoryTest extends JpaTestSupport {

    private static final Instant CREATED_AT = Instant.parse("2026-06-09T10:00:00Z");

    @Test
    @DisplayName("회원이 작성한 포스트의 작성자 연결을 모두 제거한다")
    void clearsPostAuthors() {
        // given
        Post post = postRepository.save(Post.create(
                1L, "title", "description", "550e8400-e29b-41d4-a716-446655440000.png", CREATED_AT
        ));

        // when
        long updatedCount = postRepository.clearAuthorId(1L);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(updatedCount).isEqualTo(1);
        assertThat(postRepository.findById(post.getId()).orElseThrow().getAuthorId()).isNull();
    }

    @Test
    @DisplayName("회원의 포스트별 하트 수를 조회하고 카운트를 감소시킨 뒤 하트를 삭제한다")
    void countsDecreasesAndDeletesHearts() {
        // given
        heartRepository.save(Heart.create(1L, 10L));
        heartRepository.save(Heart.create(1L, 20L));
        heartRepository.save(Heart.create(2L, 10L));
        PostHeartCount heartCount = PostHeartCount.create(10L);
        heartCount.increment();
        heartCount.increment();
        postHeartCountRepository.save(heartCount);
        entityManager.flush();
        entityManager.clear();

        // when
        List<HeartCountByPostId> counts = heartRepository.getCountsByUserId(1L);
        long decreasedCount = postHeartCountRepository.decrease(10L, 1L);
        long deletedCount = heartRepository.deleteByUserId(1L);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(counts).containsExactlyInAnyOrder(
                new HeartCountByPostId(10L, 1L),
                new HeartCountByPostId(20L, 1L)
        );
        assertThat(decreasedCount).isEqualTo(1);
        assertThat(deletedCount).isEqualTo(2);
        assertThat(postHeartCountRepository.findById(10L).orElseThrow().getCount()).isEqualTo(1);
        assertThat(heartRepository.count()).isEqualTo(1);
    }
}
