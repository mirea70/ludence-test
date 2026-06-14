package com.test.ludens.heart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.common.page.PageRequest;
import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.support.JpaTestSupport;
import com.test.ludens.user.domain.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("사용자 하트 포스트 목록 저장소 쿼리 테스트")
class UserHeartQueryRepositoryTest extends JpaTestSupport {

    private static final Instant CREATED_AT = Instant.parse("2026-06-10T10:00:00Z");

    @Test
    @DisplayName("사용자가 하트한 활성 포스트를 최신순으로 조회하고 삭제 포스트를 제외한다")
    void findsActiveHeartedPosts_inLatestOrder() {
        // given
        User viewer = userRepository.save(User.create("viewer", "encoded-password", CREATED_AT));
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        Post oldPost = savePost(author.getId(), "old", "550e8400-e29b-41d4-a716-446655440020.png", CREATED_AT);
        Post newPost = savePost(author.getId(), "new", "550e8400-e29b-41d4-a716-446655440021.png",
                CREATED_AT.plusSeconds(60));
        Post deletedPost = savePost(author.getId(), "deleted", "550e8400-e29b-41d4-a716-446655440022.png",
                CREATED_AT.plusSeconds(120));
        deletedPost.delete(CREATED_AT.plusSeconds(180));
        heartRepository.save(Heart.create(viewer.getId(), oldPost.getId()));
        heartRepository.save(Heart.create(viewer.getId(), newPost.getId()));
        heartRepository.save(Heart.create(viewer.getId(), deletedPost.getId()));
        entityManager.flush();
        entityManager.clear();

        // when
        List<PostDetailResponse> posts = postRepository.findActiveDetailsHeartedByUserId(
                viewer.getId(), new PageRequest(1, 20)
        );

        // then
        assertThat(posts).extracting(PostDetailResponse::title).containsExactly("new", "old");
        assertThat(posts).allMatch(PostDetailResponse::hearted);
        assertThat(postRepository.countActiveHeartedByUserId(viewer.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("탈퇴 작성자의 하트 포스트는 username을 null로 반환한다")
    void returnsNullUsername_whenHeartedPostAuthorWithdraws() {
        // given
        User viewer = userRepository.save(User.create("viewer", "encoded-password", CREATED_AT));
        User author = userRepository.save(User.create("author", "encoded-password", CREATED_AT));
        Post post = savePost(author.getId(), "title", "550e8400-e29b-41d4-a716-446655440023.png", CREATED_AT);
        heartRepository.save(Heart.create(viewer.getId(), post.getId()));
        post.removeAuthor();
        author.withdraw("deleted_author", CREATED_AT.plusSeconds(60));
        entityManager.flush();
        entityManager.clear();

        // when
        PostDetailResponse detail = postRepository.findActiveDetailsHeartedByUserId(
                viewer.getId(), new PageRequest(1, 20)
        ).getFirst();

        // then
        assertThat(detail.username()).isNull();
    }

    private Post savePost(Long authorId, String title, String imageKey, Instant createdAt) {
        Post post = postRepository.save(Post.create(authorId, title, null, imageKey, createdAt));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        return post;
    }
}
