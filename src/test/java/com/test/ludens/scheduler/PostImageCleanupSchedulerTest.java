package com.test.ludens.scheduler;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.post.service.PostImageCleanupService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Test
    @DisplayName("매일 정각에 포스트 이미지 정리를 실행한다")
    void schedulesCleanupAtMidnight() throws NoSuchMethodException {
        // when
        Method cleanupMethod = PostImageCleanupScheduler.class.getMethod("cleanup");

        // then
        assertThat(cleanupMethod.getAnnotation(Scheduled.class).cron()).isEqualTo("0 0 0 * * *");
    }
}
