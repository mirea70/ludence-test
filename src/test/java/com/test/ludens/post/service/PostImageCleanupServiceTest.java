package com.test.ludens.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.common.storage.ImageStorage;
import com.test.ludens.post.repository.PostCleanupCandidate;
import com.test.ludens.post.repository.PostRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.LongStream;
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

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private PostPhysicalDeleteService physicalDeleteService;

    @Test
    @DisplayName("삭제 후 30일이 지난 포스트는 이미지 삭제 후 데이터를 물리 삭제한다")
    void deletesExpiredPostData_afterImageDeletion() {
        // given
        Instant expiredAt = NOW.minusSeconds(30L * 24 * 60 * 60);
        PostCleanupCandidate candidate = new PostCleanupCandidate(1L, REFERENCED_IMAGE_KEY, expiredAt);
        given(postRepository.findCleanupCandidates(expiredAt, null, null, 100)).willReturn(List.of(candidate));

        // when
        cleanupService().cleanup();

        // then
        verify(imageStorage).delete(REFERENCED_IMAGE_KEY);
        verify(physicalDeleteService).deleteExpiredPost(1L, expiredAt);
    }

    @Test
    @DisplayName("삭제된 포스트의 이미지 삭제가 실패하면 포스트 데이터를 보존한다")
    void preservesExpiredPostData_whenImageDeletionFails() {
        // given
        Instant expiredAt = NOW.minusSeconds(30L * 24 * 60 * 60);
        PostCleanupCandidate candidate = new PostCleanupCandidate(1L, REFERENCED_IMAGE_KEY, expiredAt);
        given(postRepository.findCleanupCandidates(expiredAt, null, null, 100)).willReturn(List.of(candidate));
        doThrow(new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED))
                .when(imageStorage).delete(REFERENCED_IMAGE_KEY);

        // when
        cleanupService().cleanup();

        // then
        verifyNoInteractions(physicalDeleteService);
    }

    @Test
    @DisplayName("정리 대상이 배치 크기를 초과하면 다음 배치를 이어서 처리한다")
    void continuesCleanup_whenCandidatesExceedBatchSize() {
        // given
        Instant expiredAt = NOW.minusSeconds(30L * 24 * 60 * 60);
        List<PostCleanupCandidate> firstBatch = LongStream.rangeClosed(1, 100)
                .mapToObj(id -> new PostCleanupCandidate(id, id + ".png", expiredAt))
                .toList();
        PostCleanupCandidate nextCandidate = new PostCleanupCandidate(101L, "101.png", expiredAt);
        given(postRepository.findCleanupCandidates(expiredAt, null, null, 100)).willReturn(firstBatch);
        given(postRepository.findCleanupCandidates(expiredAt, expiredAt, 100L, 100))
                .willReturn(List.of(nextCandidate));

        // when
        cleanupService().cleanup();

        // then
        verify(imageStorage).delete("101.png");
        verify(physicalDeleteService).deleteExpiredPost(101L, expiredAt);
    }

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
                imageStorage,
                physicalDeleteService,
                Clock.fixed(NOW, ZoneOffset.UTC),
                directory.toString()
        );
    }
}
