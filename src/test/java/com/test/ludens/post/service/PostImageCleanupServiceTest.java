package com.test.ludens.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludens.post.repository.PostRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostImageCleanupService 테스트")
@ExtendWith(MockitoExtension.class)
class PostImageCleanupServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");
    private static final String REFERENCED_IMAGE_KEY = "referenced.png";
    private static final String ORPHAN_IMAGE_KEY = "orphan.png";
    private static final String FRESH_ORPHAN_IMAGE_KEY = "fresh-orphan.png";

    @TempDir
    Path directory;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글에 참조되지 않고 1일이 지난 이미지 파일만 삭제한다")
    void deletesOnlyExpiredOrphanImages() throws Exception {
        // given
        Path referencedImage = Files.write(directory.resolve(REFERENCED_IMAGE_KEY), new byte[] {1});
        Path orphanImage = Files.write(directory.resolve(ORPHAN_IMAGE_KEY), new byte[] {2});
        Path freshOrphanImage = Files.write(directory.resolve(FRESH_ORPHAN_IMAGE_KEY), new byte[] {3});
        Files.setLastModifiedTime(referencedImage, FileTime.from(NOW.minusSeconds(172800)));
        Files.setLastModifiedTime(orphanImage, FileTime.from(NOW.minusSeconds(172801)));
        Files.setLastModifiedTime(freshOrphanImage, FileTime.from(NOW.minusSeconds(3600)));
        given(postRepository.findAllImageKeys()).willReturn(List.of(REFERENCED_IMAGE_KEY));
        PostImageCleanupService cleanupService = cleanupService();

        // when
        cleanupService.cleanup();

        // then
        assertThat(Files.exists(referencedImage)).isTrue();
        assertThat(Files.exists(orphanImage)).isFalse();
        assertThat(Files.exists(freshOrphanImage)).isTrue();
        verify(postRepository).findAllImageKeys();
    }

    private PostImageCleanupService cleanupService() {
        return new PostImageCleanupService(
                postRepository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                directory.toString()
        );
    }
}
