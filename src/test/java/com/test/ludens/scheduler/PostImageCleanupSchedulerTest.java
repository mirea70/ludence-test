package com.test.ludens.scheduler;

import static org.mockito.Mockito.verify;

import com.test.ludens.post.service.PostImageCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostImageCleanupScheduler 테스트")
@ExtendWith(MockitoExtension.class)
class PostImageCleanupSchedulerTest {

    @Mock
    private PostImageCleanupService cleanupService;

    @Test
    @DisplayName("스케줄러는 이미지 정리 서비스를 위임한다")
    void delegatesToCleanupService() {
        // when
        new PostImageCleanupScheduler(cleanupService).cleanup();

        // then
        verify(cleanupService).cleanup();
    }
}
