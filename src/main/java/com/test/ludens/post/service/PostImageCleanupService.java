package com.test.ludens.post.service;

import com.test.ludens.common.storage.ImageStorage;
import com.test.ludens.post.repository.PostCleanupCandidate;
import com.test.ludens.post.repository.PostRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostImageCleanupService {

    private static final int ORPHAN_IMAGE_RETENTION_DAYS = 2;
    private static final int DELETED_POST_RETENTION_DAYS = 30;
    private static final int CLEANUP_BATCH_SIZE = 100;

    private final PostRepository postRepository;
    private final ImageStorage imageStorage;
    private final PostPhysicalDeleteService physicalDeleteService;
    private final Clock clock;
    private final Path imageDirectory;

    public PostImageCleanupService(
            PostRepository postRepository,
            ImageStorage imageStorage,
            PostPhysicalDeleteService physicalDeleteService,
            Clock clock,
            @Value("${storage.image-directory}") String imageDirectory
    ) {
        this.postRepository = postRepository;
        this.imageStorage = imageStorage;
        this.physicalDeleteService = physicalDeleteService;
        this.clock = clock;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    public void cleanup() {
        cleanupExpiredDeletedPosts();
        cleanupOrphanImages();
    }

    private void cleanupExpiredDeletedPosts() {
        Instant expiredAt = clock.instant().minus(DELETED_POST_RETENTION_DAYS, ChronoUnit.DAYS);
        Instant cursorDeletedAt = null;
        Long cursorPostId = null;

        while (true) {
            List<PostCleanupCandidate> candidates = postRepository.findCleanupCandidates(
                    expiredAt,
                    cursorDeletedAt,
                    cursorPostId,
                    CLEANUP_BATCH_SIZE
            );
            candidates.forEach(candidate -> deleteExpiredPost(candidate, expiredAt));
            if (candidates.size() < CLEANUP_BATCH_SIZE) {
                return;
            }
            PostCleanupCandidate lastCandidate = candidates.getLast();
            cursorDeletedAt = lastCandidate.deletedAt();
            cursorPostId = lastCandidate.postId();
        }
    }

    private void deleteExpiredPost(PostCleanupCandidate candidate, Instant expiredAt) {
        try {
            imageStorage.delete(candidate.imageKey());
            physicalDeleteService.deleteExpiredPost(candidate.postId(), expiredAt);
        } catch (RuntimeException exception) {
            log.error("Failed to cleanup expired post: postId={}", candidate.postId(), exception);
        }
    }

    private void cleanupOrphanImages() {
        if (!Files.exists(imageDirectory)) {
            return;
        }

        Set<String> referencedImageKeys = new HashSet<>(postRepository.findAllImageKeys());
        Instant expiredAt = clock.instant().minus(ORPHAN_IMAGE_RETENTION_DAYS, ChronoUnit.DAYS);

        try (Stream<Path> paths = Files.list(imageDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> isExpiredOrphan(path, referencedImageKeys, expiredAt))
                    .forEach(this::deleteQuietly);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to cleanup orphan post images.", exception);
        }
    }

    private boolean isExpiredOrphan(Path path, Set<String> referencedImageKeys, Instant expiredAt) {
        String fileName = path.getFileName().toString();
        if (referencedImageKeys.contains(fileName)) {
            return false;
        }

        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return lastModifiedTime.toInstant().isBefore(expiredAt);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect image file.", exception);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete orphan image file.", exception);
        }
    }
}
