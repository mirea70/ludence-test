package com.test.ludence.post.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.domain.vo.PostDescription;
import com.test.ludence.post.domain.vo.PostImageKey;
import com.test.ludence.post.domain.vo.PostTitle;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(name = "posts")
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

    public void delete(Instant deletedAt) {
        validateActive();
        validateTime(deletedAt);
        this.deletedAt = deletedAt;
    }

    public void removeAuthor() {
        authorId = null;
    }

    private void validateActive() {
        if (!isActive()) {
            throw new DomainException(PostErrorInfo.ALREADY_DELETED);
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
