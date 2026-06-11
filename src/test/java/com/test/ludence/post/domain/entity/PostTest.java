package com.test.ludence.post.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Post 도메인 테스트")
class PostTest {

    private static final String IMAGE_KEY = "550e8400-e29b-41d4-a716-446655440000.png";

    @Test
    @DisplayName("포스트 생성 시 작성자와 내용과 생성 시각을 저장하고 활성 상태가 된다")
    void createsActivePost_whenValuesAreValid() {
        // given
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");

        // when
        Post post = Post.create(1L, "my post", "description", IMAGE_KEY, createdAt);

        // then
        assertThat(post.getAuthorId()).isEqualTo(1L);
        assertThat(post.getTitle()).isEqualTo("my post");
        assertThat(post.getDescription()).isEqualTo("description");
        assertThat(post.getImageKey()).isEqualTo(IMAGE_KEY);
        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
        assertThat(post.getEditedAt()).isEqualTo(createdAt);
        assertThat(post.isActive()).isTrue();
    }

    @Test
    @DisplayName("포스트 수정 시 제목과 설명과 수정 시각이 변경된다")
    void updatesPostContent_whenPostIsActive() {
        // given
        Post post = Post.create(1L, "before", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        Instant editedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        post.update("after", "updated", editedAt);

        // then
        assertThat(post.getTitle()).isEqualTo("after");
        assertThat(post.getDescription()).isEqualTo("updated");
        assertThat(post.getEditedAt()).isEqualTo(editedAt);
    }

    @Test
    @DisplayName("포스트 삭제 시 삭제 시각을 기록하고 비활성 상태가 된다")
    void recordsDeletedAt_whenPostIsDeleted() {
        // given
        Post post = Post.create(1L, "title", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        Instant deletedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        post.delete(deletedAt);

        // then
        assertThat(post.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(post.isActive()).isFalse();
    }

    @Test
    @DisplayName("삭제된 포스트를 수정하면 DomainException이 발생한다")
    void throwsDomainException_whenDeletedPostIsUpdated() {
        // given
        Post post = Post.create(1L, "title", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        post.delete(Instant.parse("2026-06-10T10:00:00Z"));

        // when & then
        assertThatThrownBy(() -> post.update("updated", null, Instant.parse("2026-06-11T10:00:00Z")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("작성자 참조를 제거하면 작성자 ID가 null이 된다")
    void removesAuthorReference_whenAuthorWithdraws() {
        // given
        Post post = Post.create(1L, "title", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));

        // when
        post.removeAuthor();

        // then
        assertThat(post.getAuthorId()).isNull();
    }

    @Test
    @DisplayName("작성자가 일치하면 포스트를 수정할 수 있다")
    void updatesPost_whenAuthorMatches() {
        // given
        Post post = Post.create(1L, "before", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        Instant editedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        post.updateByAuthor(1L, "after", "updated", editedAt);

        // then
        assertThat(post.getTitle()).isEqualTo("after");
        assertThat(post.getDescription()).isEqualTo("updated");
        assertThat(post.getEditedAt()).isEqualTo(editedAt);
    }

    @Test
    @DisplayName("작성자가 일치하지 않으면 포스트를 수정할 수 없다")
    void throwsDomainException_whenAuthorDoesNotMatch() {
        // given
        Post post = Post.create(1L, "before", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));

        // when & then
        assertThatThrownBy(() -> post.updateByAuthor(
                2L, "after", "updated", Instant.parse("2026-06-10T10:00:00Z")
        )).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("작성자가 탈퇴한 포스트는 수정할 수 없다")
    void throwsDomainException_whenPostAuthorWasRemoved() {
        // given
        Post post = Post.create(1L, "before", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        post.removeAuthor();

        // when & then
        assertThatThrownBy(() -> post.updateByAuthor(
                1L, "after", null, Instant.parse("2026-06-10T10:00:00Z")
        )).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("제목이 null이고 설명만 전달되면 설명만 수정한다")
    void updatesOnlyDescription_whenTitleIsNull() {
        // given
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
        Instant editedAt = Instant.parse("2026-06-10T10:00:00Z");
        Post post = Post.create(1L, "title", "before", IMAGE_KEY, createdAt);

        // when
        post.patchByAuthor(1L, null, "after", editedAt);

        // then
        assertThat(post.getTitle()).isEqualTo("title");
        assertThat(post.getDescription()).isEqualTo("after");
        assertThat(post.getEditedAt()).isEqualTo(editedAt);
    }

    @Test
    @DisplayName("설명이 빈 문자열이면 설명을 제거한다")
    void removesDescription_whenDescriptionIsBlank() {
        // given
        Post post = Post.create(1L, "title", "description", IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));

        // when
        post.patchByAuthor(1L, null, " ", Instant.parse("2026-06-10T10:00:00Z"));

        // then
        assertThat(post.getDescription()).isNull();
    }

    @Test
    @DisplayName("제목과 설명에 변경값이 없으면 수정 시각을 유지한다")
    void preservesEditedAt_whenNoChangeValueExists() {
        // given
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
        Post post = Post.create(1L, "title", "description", IMAGE_KEY, createdAt);

        // when
        post.patchByAuthor(1L, " ", null, Instant.parse("2026-06-10T10:00:00Z"));

        // then
        assertThat(post.getTitle()).isEqualTo("title");
        assertThat(post.getDescription()).isEqualTo("description");
        assertThat(post.getEditedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("작성자가 포스트를 삭제하면 삭제 시각을 기록한다")
    void deletesPost_whenAuthorMatches() {
        // given
        Post post = Post.create(1L, "title", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));
        Instant deletedAt = Instant.parse("2026-06-10T10:00:00Z");

        // when
        post.deleteByAuthor(1L, deletedAt);

        // then
        assertThat(post.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("작성자가 아니면 포스트를 삭제할 수 없다")
    void throwsDomainException_whenNonAuthorDeletesPost() {
        // given
        Post post = Post.create(1L, "title", null, IMAGE_KEY, Instant.parse("2026-06-09T10:00:00Z"));

        // when & then
        assertThatThrownBy(() -> post.deleteByAuthor(2L, Instant.parse("2026-06-10T10:00:00Z")))
                .isInstanceOf(DomainException.class);
    }
}
