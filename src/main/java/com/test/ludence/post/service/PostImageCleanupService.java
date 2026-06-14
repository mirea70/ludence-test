package com.test.ludence.post.service;

import com.test.ludence.post.repository.PostRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PostImageCleanupService {

    private static final int ORPHAN_IMAGE_RETENTION_DAYS = 2;

    private final PostRepository postRepository;
    private final Clock clock;
    private final Path imageDirectory;

    public PostImageCleanupService(
            PostRepository postRepository,
            Clock clock,
            @Value("${storage.image-directory}") String imageDirectory
    ) {
        this.postRepository = postRepository;
        this.clock = clock;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    public void cleanup() {
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
