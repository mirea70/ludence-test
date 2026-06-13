package com.test.ludence.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("AsyncConfig 테스트")
class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    @DisplayName("조회수와 사용자 활동에 서로 다른 실행기를 생성한다")
    void createsSeparateExecutorsForViewCountsAndUserActivities() {
        // when
        Executor postViewCountExecutor = asyncConfig.postViewCountTaskExecutor();
        Executor userActivityExecutor = asyncConfig.userActivityTaskExecutor();

        // then
        assertThat(postViewCountExecutor).isNotSameAs(userActivityExecutor);
        assertExecutor((ThreadPoolTaskExecutor) postViewCountExecutor, "post-view-count-");
        assertExecutor((ThreadPoolTaskExecutor) userActivityExecutor, "user-activity-");
    }

    private void assertExecutor(ThreadPoolTaskExecutor executor, String threadNamePrefix) {
        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaxPoolSize()).isEqualTo(4);
        assertThat(executor.getQueueCapacity()).isEqualTo(500);
        assertThat(executor.getThreadNamePrefix()).isEqualTo(threadNamePrefix);
        assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
        executor.shutdown();
    }
}
