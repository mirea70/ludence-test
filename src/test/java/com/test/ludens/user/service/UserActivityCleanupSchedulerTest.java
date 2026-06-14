package com.test.ludens.user.service;

import static org.mockito.Mockito.verify;

import com.test.ludens.scheduler.UserActivityCleanupScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserActivityCleanupScheduler 테스트")
@ExtendWith(MockitoExtension.class)
class UserActivityCleanupSchedulerTest {

    @Mock
    private UserActivityCleanupService cleanupService;

    @Test
    @DisplayName("주기 실행 시 사용자 행동 이력을 정리한다")
    void cleansUpUserActivities() {
        // when
        new UserActivityCleanupScheduler(cleanupService).cleanup();

        // then
        verify(cleanupService).cleanup();
    }
}
