package com.test.ludence.heart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.common.page.PageRequest;
import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.repository.PostHeartAccess;
import com.test.ludence.support.JpaTestSupport;
import com.test.ludence.user.domain.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("포스트 하트 회원 목록 저장소 쿼리 테스트")
class HeartQueryRepositoryTest extends JpaTestSupport {

    private static final Instant CREATED_AT = Instant.parse("2026-06-10T10:00:00Z");

    @Test
    @DisplayName("활성 하트 회원을 username 오름차순으로 페이지 조회한다")
    void findsActiveHeartUsers_inUsernameOrder() {
        // given
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        User oldUser = userRepository.save(User.create("alpha", "encoded-password", CREATED_AT.plusSeconds(10)));
        User newUser = userRepository.save(User.create("zeta", "encoded-password", CREATED_AT.plusSeconds(20)));
        User withdrawnUser = userRepository.save(User.create("withdrawn", "encoded-password", CREATED_AT.plusSeconds(30)));
        Post post = postRepository.save(Post.create(
                author.getId(), "title", null, "550e8400-e29b-41d4-a716-446655440010.png", CREATED_AT
        ));
        heartRepository.save(Heart.create(oldUser.getId(), post.getId()));
        heartRepository.save(Heart.create(newUser.getId(), post.getId()));
        heartRepository.save(Heart.create(withdrawnUser.getId(), post.getId()));
        withdrawnUser.withdraw("deleted_withdrawn", CREATED_AT.plusSeconds(40));
        entityManager.flush();
        entityManager.clear();

        // when
        List<String> users = heartRepository.findActiveUsernamesByPostId(post.getId(), new PageRequest(1, 1));

        // then
        assertThat(users).containsExactly("alpha");
    }

    @Test
    @DisplayName("작성자가 탈퇴한 활성 포스트도 작성자 ID projection을 반환한다")
    void findsActivePostAuthorProjection_whenAuthorIdIsNull() {
        // given
        Post post = postRepository.save(Post.create(
                1L, "title", null, "550e8400-e29b-41d4-a716-446655440011.png", CREATED_AT
        ));
        PostHeartCount heartCount = PostHeartCount.create(post.getId());
        heartCount.increment();
        postHeartCountRepository.save(heartCount);
        post.removeAuthor();
        entityManager.flush();
        entityManager.clear();

        // when
        PostHeartAccess access = postRepository.findActiveHeartAccessById(post.getId()).orElseThrow();

        // then
        assertThat(access.authorId()).isNull();
        assertThat(access.total()).isEqualTo(1);
    }
}
