package com.test.ludens.post.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.post.domain.vo.PostDescription;
import com.test.ludens.post.domain.vo.PostImageKey;
import com.test.ludens.post.domain.vo.PostTitle;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "posts",
        indexes = @Index(
                name = "idx_posts_author_created",
                columnList = "author_id, created_at DESC, id DESC"
        ),
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_posts_image_key",
                        columnNames = {"image_key"}
                )
        }
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;

    @Embedded
    private PostTitle title;

    @Embedded
    private PostDescription description;

    @Embedded
    private PostImageKey imageKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant editedAt;

    private Instant deletedAt;

    protected Post() {
    }

    private Post(
            Long authorId,
            PostTitle title,
            PostDescription description,
            PostImageKey imageKey,
            Instant createdAt,
            Instant editedAt
    ) {
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.imageKey = imageKey;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
    }

    public static Post create(Long authorId, String title, String description, String imageKey, Instant createdAt) {
        validateAuthorId(authorId);
        validateTime(createdAt);
        return new Post(
                authorId,
                new PostTitle(title),
                new PostDescription(description),
                new PostImageKey(imageKey),
                createdAt,
                createdAt
        );
    }

    public String getTitle() {
        return title.value();
    }

    public String getDescription() {
        return description.value();
    }

    public String getImageKey() {
        return imageKey.value();
    }

    @JsonIgnore
    public boolean isActive() {
        return deletedAt == null;
    }

    public void update(String title, String description, Instant editedAt) {
        validateActive();
        validateTime(editedAt);
        this.title = new PostTitle(title);
        this.description = new PostDescription(description);
        this.editedAt = editedAt;
    }

    public void updateByAuthor(Long authorId, String title, String description, Instant editedAt) {
        validateAuthor(authorId);
        update(title, description, editedAt);
    }

    public void patchByAuthor(Long authorId, String title, String description, Instant editedAt) {
        validateAuthor(authorId);
        validateActive();
        validateTime(editedAt);

        boolean changed = patchTitle(title);
        changed |= patchDescription(description);

        if (changed) {
            this.editedAt = editedAt;
        }
    }

    public void delete(Instant deletedAt) {
        validateActive();
        validateTime(deletedAt);
        this.deletedAt = deletedAt;
    }

    public void deleteByAuthor(Long authorId, Instant deletedAt) {
        validateAuthor(authorId);
        delete(deletedAt);
    }

    public void removeAuthor() {
        authorId = null;
    }

    private boolean patchTitle(String title) {
        if (title == null || title.isBlank() || Objects.equals(getTitle(), title)) {
            return false;
        }
        this.title = new PostTitle(title);
        return true;
    }

    private boolean patchDescription(String description) {
        if (description == null) {
            return false;
        }

        String normalizedDescription = description.isBlank() ? null : description;
        if (Objects.equals(getDescription(), normalizedDescription)) {
            return false;
        }
        this.description = new PostDescription(normalizedDescription);
        return true;
    }

    private void validateActive() {
        if (!isActive()) {
            throw new DomainException(PostErrorInfo.ALREADY_DELETED);
        }
    }

    private void validateAuthor(Long authorId) {
        if (!Objects.equals(this.authorId, authorId)) {
            throw new DomainException(PostErrorInfo.FORBIDDEN);
        }
    }

    private static void validateAuthorId(Long authorId) {
        if (authorId == null || authorId < 1) {
            throw new DomainException(PostErrorInfo.INVALID_AUTHOR_ID);
        }
    }

    private static void validateTime(Instant time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null일 수 없습니다.");
        }
    }
}
