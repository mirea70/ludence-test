package com.test.ludens.scheduler;

import com.test.ludens.user.service.UserActivityCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityCleanupScheduler {

    private final UserActivityCleanupService cleanupService;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanup() {
        cleanupService.cleanup();
    }
}
